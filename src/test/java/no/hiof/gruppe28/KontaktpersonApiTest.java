package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.repository.DemoNyhetRepository;
import no.hiof.gruppe28.repository.KontaktpersonRepository;
import no.hiof.gruppe28.service.KontaktpersonService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.LokallagService;
import no.hiof.gruppe28.service.NyhetService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.List;

import static no.hiof.gruppe28.TestData.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KontaktpersonApiTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private Javalin startMed(KontaktpersonRepository kontaktpersonRepository) {
        return Application.create(
            new KursService(repository(List.of()), Clock.systemUTC()),
            null,
            new NyhetService(new DemoNyhetRepository()),
            null,
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(kontaktpersonRepository)
        ).start(0);
    }

    private HttpResponse<String> get(Javalin app, String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void girPersoneneMedFelteneKontaktsidenTrenger() throws Exception {
        KontaktpersonRepository toPersoner = () -> List.of(
            new Kontaktperson(1, "Kari Hansen", "kari@test.no", "911 23 456", "kari.jpg", "Kursansvarlig", "Fredrikstad"),
            new Kontaktperson(2, "Per Kristiansen", "per@test.no", "933 45 678", "per.jpg", null, null)
        );
        Javalin app = startMed(toPersoner);
        try {
            var svar = get(app, "/api/kontaktpersoner");
            JsonNode personer = json.readTree(svar.body());

            assertEquals(200, svar.statusCode());
            assertEquals(2, personer.size());
            assertEquals("Kari Hansen", personer.get(0).get("navn").asText());
            assertEquals("911 23 456", personer.get(0).get("telefon").asText());
            assertEquals("kari@test.no", personer.get(0).get("epost").asText());
            assertEquals("Kursansvarlig", personer.get(0).get("verv").asText());
            assertEquals("Fredrikstad", personer.get(0).get("sted").asText());
            assertEquals("Per Kristiansen", personer.get(1).get("navn").asText());
        } finally {
            app.stop();
        }
    }

    @Test
    void visPaKontaktsideSendesIkkeTilFrontend() throws Exception {
        KontaktpersonRepository enPerson = () -> List.of(
            new Kontaktperson(1, "Kari Hansen", "kari@test.no", "911 23 456", "kari.jpg", "Leder", "Østfold")
        );
        Javalin app = startMed(enPerson);
        try {
            JsonNode personer = json.readTree(get(app, "/api/kontaktpersoner").body());

            assertFalse(personer.get(0).has("vis_pa_kontaktside"));
            assertFalse(personer.get(0).has("visPaKontaktside"));
        } finally {
            app.stop();
        }
    }

    @Test
    void ingenPersonerGirTomListe() throws Exception {
        KontaktpersonRepository ingen = () -> List.of();
        Javalin app = startMed(ingen);
        try {
            var svar = get(app, "/api/kontaktpersoner");

            assertEquals(200, svar.statusCode());
            assertEquals("[]", svar.body());
        } finally {
            app.stop();
        }
    }
}

