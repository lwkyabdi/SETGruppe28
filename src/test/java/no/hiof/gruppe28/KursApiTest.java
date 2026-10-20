package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.repository.DatabaseException;
import no.hiof.gruppe28.repository.KursRepository;
import no.hiof.gruppe28.model.Kurs;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.List;
import java.util.Optional;
import static no.hiof.gruppe28.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KursApiTest {
    private Javalin app;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @BeforeAll void start() {
        var date = LocalDate.of(2026, 10, 17);
        var data = List.of(kurs(1, date.plusDays(3)), kurs(2, date.minusDays(2)), kurs(3, date));
        var clock = Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneId.of("Europe/Oslo"));
        app = Application.create(new KursService(repository(data), clock)).start(0);
    }
    @AfterAll void stop() { if (app != null) app.stop(); }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
    @Test void healthSvar() throws Exception {
        var r = get("/api/health");
        assertEquals(200, r.statusCode());
        assertEquals("OK", json.readTree(r.body()).get("status").asText());
    }
    @Test void oversiktHarAvtaltJsonFormat() throws Exception {
        var r = get("/api/kurs");
        assertEquals(200, r.statusCode());
        JsonNode body = json.readTree(r.body());
        assertEquals(3, body.size());
        assertEquals("2026-10-20", body.get(0).get("dato").asText());
        assertEquals(100, body.get(0).get("medlemspris").asInt());
        assertEquals("Testkontakt", body.get(0).get("kontakt").get("navn").asText());
    }
    @Test void kommendeErEgenRuteOgBrukerDagensDato() throws Exception {
        var r = get("/api/kurs/kommende");
        assertEquals(200, r.statusCode());
        var body = json.readTree(r.body());
        assertEquals(2, body.size());
        assertEquals(3, body.get(0).get("id").asInt());
        assertEquals(1, body.get(1).get("id").asInt());
    }
    @Test void detaljerAvhengerAvId() throws Exception {
        assertEquals("Kurs 1", json.readTree(get("/api/kurs/1").body()).get("tittel").asText());
        assertEquals("Kurs 3", json.readTree(get("/api/kurs/3").body()).get("tittel").asText());
    }
    @Test void ukjentKursGir404() throws Exception {
        assertEquals(404, get("/api/kurs/99999").statusCode());
    }
    @Test void ugyldigIdGir400() throws Exception {
        assertEquals(400, get("/api/kurs/abc").statusCode());
        assertEquals(400, get("/api/kurs/-1").statusCode());
    }
    @Test void frontendOgVueServeres() throws Exception {
        assertEquals(200, get("/").statusCode());
        assertEquals(200, get("/index.html").statusCode());
        assertEquals(200, get("/style.css").statusCode());
        assertEquals(200, get("/webjars/vue/3.5.41/dist/vue.global.prod.js").statusCode());
    }
    @Test void databasefeilGir503MedForstaeligMelding() throws Exception {
        KursRepository failing = new KursRepository() {
            public List<Kurs> findPubliserte() { throw new DatabaseException(new RuntimeException("Intern forbindelsesfeil")); }
            public Optional<Kurs> findPublisert(long id) { return Optional.empty(); }
        };
        Javalin failedApp = Application.create(new KursService(failing, Clock.systemUTC())).start(0);
        try {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + failedApp.port() + "/api/kurs")).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(503, response.statusCode());
            assertTrue(json.readTree(response.body()).get("melding").asText().contains("Prøv igjen"));
            assertFalse(response.body().contains("Intern forbindelsesfeil"));
        } finally { failedApp.stop(); }
    }
}
