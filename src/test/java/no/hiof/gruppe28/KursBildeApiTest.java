package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.service.KursService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static no.hiof.gruppe28.TestData.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KursBildeApiTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private Javalin app;

    private final Kontaktperson kontakt = new Kontaktperson(1, "Kari", "kari@test.no", "900 00 000", "kari.jpg");

    private final Kurs medBilde = new Kurs(
        1, "Strikkekurs", "Beskrivelse", LocalDate.of(2026, 12, 1), "18:00", "2 timer",
        "Fredrikstad", "Fredrikstad Husflidslag", "strikking", "Voksne", "Nybegynner", "Pinner",
        new BigDecimal("100"), new BigDecimal("200"), 10, 10, kontakt,
        "kurs-1.jpg", "Hender som strikker");

    private final Kurs utenBilde = new Kurs(
        2, "Vevkurs", "Beskrivelse", LocalDate.of(2026, 12, 2), "18:00", "2 timer",
        "Moss", "Moss Husflidslag", "veving", "Voksne", "Nybegynner", "Ingen",
        new BigDecimal("100"), new BigDecimal("200"), 8, 8, kontakt);

    @BeforeEach
    void start() {
        var clock = Clock.systemUTC();
        app = Application.create(new KursService(repository(List.of(medBilde, utenBilde)), clock)).start(0);
    }

    @AfterEach
    void stopp() {
        app.stop();
    }

    private JsonNode hentKurs(long id) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + "/api/kurs/" + id)).GET().build();
        return json.readTree(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
    }

    @Test
    void kursMedBildeSenderBildestiOgBildealt() throws Exception {
        JsonNode kurs = hentKurs(1);

        assertEquals("kurs-1.jpg", kurs.get("bildesti").asText());
        assertEquals("Hender som strikker", kurs.get("bildealt").asText());
    }

    @Test
    void kursMedDeGamle17VerdieneVirkerFortsatt() throws Exception {
        JsonNode kurs = hentKurs(2);

        assertEquals("Vevkurs", kurs.get("tittel").asText());
        assertTrue(kurs.get("bildesti").isNull());
    }

    @Test
    void deGamleFelteneHeterDetSammeSomFor() throws Exception {
        JsonNode kurs = hentKurs(1);

        assertTrue(kurs.has("ledigePlasser"));
        assertEquals("Kari", kurs.get("kontakt").get("navn").asText());
    }
}
