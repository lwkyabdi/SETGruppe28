package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoPameldingApiTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private Javalin app;

    @BeforeEach
    void start() throws Exception {
        app = Application.create("--demo").start(0);
    }

    @AfterEach
    void stopp() {
        app.stop();
    }

    private String pamelding(String nokkel, String epost, boolean samtykke) {
        return """
            {"nokkel": "%s", "fornavn": "Kari", "etternavn": "Nordmann",
             "epost": "%s", "telefon": "90012345", "fodselsar": 1990,
             "medlem": "ja", "medlemsnummer": "", "kommentar": "", "samtykke": %s}
            """.formatted(nokkel, epost, samtykke);
    }

    private HttpResponse<String> meldPa(long kursId, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(
                "http://localhost:" + app.port() + "/api/kurs/" + kursId + "/pameldinger"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void kursMedPlassGirPameldt() throws Exception {
        var svar = meldPa(1, pamelding(UUID.randomUUID().toString(), "kari@test.no", true));
        JsonNode kvittering = json.readTree(svar.body());

        assertEquals(201, svar.statusCode());
        assertEquals("pameldt", kvittering.get("status").asText());
    }

    @Test
    void fulltKursGirVenteliste() throws Exception {
        var svar = meldPa(2, pamelding(UUID.randomUUID().toString(), "kari@test.no", true));

        assertEquals(201, svar.statusCode());
        assertEquals("venteliste", json.readTree(svar.body()).get("status").asText());
    }

    @Test
    void sammeNokkelToGangerGirSammeReferanse() throws Exception {
        String nokkel = UUID.randomUUID().toString();

        var forste = json.readTree(meldPa(1, pamelding(nokkel, "kari@test.no", true)).body());
        var andre = json.readTree(meldPa(1, pamelding(nokkel, "kari@test.no", true)).body());

        assertEquals(forste.get("referanse").asText(), andre.get("referanse").asText());
        assertEquals(forste.get("ledigePlasser").asInt(), andre.get("ledigePlasser").asInt());
    }

    @Test
    void sammeEpostMedNyNokkelGir409() throws Exception {
        meldPa(1, pamelding(UUID.randomUUID().toString(), "kari@test.no", true));
        var svar = meldPa(1, pamelding(UUID.randomUUID().toString(), "kari@test.no", true));

        assertEquals(409, svar.statusCode());
    }

    @Test
    void kursSomHarVaertGir409() throws Exception {
        var svar = meldPa(5, pamelding(UUID.randomUUID().toString(), "kari@test.no", true));

        assertEquals(409, svar.statusCode());
    }

    @Test
    void utenSamtykkeGir400() throws Exception {
        var svar = meldPa(1, pamelding(UUID.randomUUID().toString(), "kari@test.no", false));

        assertEquals(400, svar.statusCode());
    }
}
