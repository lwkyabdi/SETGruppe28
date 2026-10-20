package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DemoModeTest {
    private Javalin app;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @BeforeAll void start() throws Exception {
        app = Application.create("--demo").start(0);
    }

    @AfterAll void stop() {
        if (app != null) app.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path))
                .GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test void demoGirKursOgDetaljerUtenDatabase() throws Exception {
        var oversikt = get("/api/kurs");
        assertEquals(200, oversikt.statusCode());
        assertEquals(5, json.readTree(oversikt.body()).size());

        var detalj = get("/api/kurs/2");
        assertEquals(200, detalj.statusCode());
        assertEquals("Strikk din egen genser", json.readTree(detalj.body()).get("tittel").asText());
        assertEquals(404, get("/api/kurs/99999").statusCode());
    }

    @Test void demoKommendeUtelaterTidligereKursOgSorterer() throws Exception {
        var response = get("/api/kurs/kommende");
        assertEquals(200, response.statusCode());
        JsonNode kurs = json.readTree(response.body());
        assertEquals(4, kurs.size());
        LocalDate forrigeDato = LocalDate.now(ZoneId.of("Europe/Oslo"));
        for (JsonNode k : kurs) {
            LocalDate dato = LocalDate.parse(k.get("dato").asText());
            assertFalse(dato.isBefore(forrigeDato));
            assertNotEquals(5, k.get("id").asInt());
            forrigeDato = dato;
        }
    }
}
