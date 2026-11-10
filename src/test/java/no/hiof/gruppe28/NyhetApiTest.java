package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.Artikkel;
import no.hiof.gruppe28.repository.ArtikkelRepository;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.service.KontaktpersonService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.LokallagService;
import no.hiof.gruppe28.service.NyhetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static no.hiof.gruppe28.TestData.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NyhetApiTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private Javalin app;

    private final List<Artikkel> artikler = List.of(
        new Artikkel(1, "Vevkurs i Moss", "Kort ingress", "Første avsnitt.\n\nAndre avsnitt.",
            "nyhet-2.jpg", "Plakat", 4L, "Moss Husflidslag", LocalDateTime.of(2026, 9, 5, 10, 0)),
        new Artikkel(2, "Høstprogrammet", "Ingress", "Tekst.",
            null, "", null, null, LocalDateTime.of(2026, 8, 28, 10, 0))
    );

    private final ArtikkelRepository falskRepository = new ArtikkelRepository() {
        public List<Artikkel> finnPubliserte() {
            return artikler;
        }

        public Optional<Artikkel> finnPublisert(long id) {
            return artikler.stream().filter(a -> a.id() == id).findFirst();
        }
    };

    @BeforeEach
    void start() {
        app = Application.create(
            new KursService(repository(List.of()), Clock.systemUTC()),
            null,
            new NyhetService(falskRepository),
            null,
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(new DemoKontaktpersonRepository())
        ).start(0);
    }

    @AfterEach
    void stopp() {
        app.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void nyhetFraEtLagHarDatabasensFeltOgNorskDato() throws Exception {
        var svar = get("/api/nyheter/1");
        JsonNode nyhet = json.readTree(svar.body());

        assertEquals(200, svar.statusCode());
        assertEquals("Vevkurs i Moss", nyhet.get("tittel").asText());
        assertEquals("Første avsnitt.\n\nAndre avsnitt.", nyhet.get("innhold").asText());
        assertEquals("nyhet-2.jpg", nyhet.get("bildesti").asText());
        assertEquals("Plakat", nyhet.get("bildealt").asText());
        assertEquals(4, nyhet.get("lokallagId").asLong());
        assertEquals("Moss Husflidslag", nyhet.get("lokallag").asText());
        assertEquals("2026-09-05", nyhet.get("dato").asText());
        assertEquals("5. september 2026", nyhet.get("datoTekst").asText());
    }

    @Test
    void nyhetUtenLagViserHeleFylket() throws Exception {
        JsonNode nyhet = json.readTree(get("/api/nyheter/2").body());

        assertEquals("Østfold Husflidslag", nyhet.get("lokallag").asText());
        assertTrue(nyhet.get("lokallagId").isNull());
    }

    @Test
    void interneFeltSendesIkke() throws Exception {
        JsonNode nyhet = json.readTree(get("/api/nyheter/1").body());

        assertFalse(nyhet.has("status"));
        assertFalse(nyhet.has("opprettet"));
        assertFalse(nyhet.has("publisertTid"));
    }

    @Test
    void ukjentNyhetGir404() throws Exception {
        var svar = get("/api/nyheter/99");

        assertEquals(404, svar.statusCode());
        assertEquals("Nyheten finnes ikke.", json.readTree(svar.body()).get("melding").asText());
    }

    @Test
    void ugyldigIdGir400() throws Exception {
        assertEquals(400, get("/api/nyheter/abc").statusCode());
    }
}

