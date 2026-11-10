package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.Lokallag;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoArtikkelRepository;
import no.hiof.gruppe28.repository.LokallagRepository;
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

class LokallagApiTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private Javalin startMed(LokallagRepository lokallagRepository) {
        return Application.create(
            new KursService(repository(List.of()), Clock.systemUTC()),
            null,
            new NyhetService(new DemoArtikkelRepository()),
            null,
            new LokallagService(lokallagRepository),
            new KontaktpersonService(new DemoKontaktpersonRepository())
        ).start(0);
    }

    private HttpResponse<String> get(Javalin app, String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void girAlleLageneMedSlugOgNavn() throws Exception {
        LokallagRepository toLag = () -> List.of(
            new Lokallag(1, "fredrikstad", "Fredrikstad Husflidslag", "Beskrivelse", null, null, null, null),
            new Lokallag(2, "moss", "Moss Husflidslag", null, null, null, null, null)
        );
        Javalin app = startMed(toLag);
        try {
            var svar = get(app, "/api/lokallag");
            JsonNode lag = json.readTree(svar.body());

            assertEquals(200, svar.statusCode());
            assertEquals(2, lag.size());
            assertEquals("fredrikstad", lag.get(0).get("slug").asText());
            assertEquals("Fredrikstad Husflidslag", lag.get(0).get("navn").asText());
            assertEquals("moss", lag.get(1).get("slug").asText());
        } finally {
            app.stop();
        }
    }

    @Test
    void aktivSendesIkkeTilFrontend() throws Exception {
        LokallagRepository ettLag = () -> List.of(
            new Lokallag(1, "halden", "Halden Husflidslag", null, null, null, null, null)
        );
        Javalin app = startMed(ettLag);
        try {
            JsonNode lag = json.readTree(get(app, "/api/lokallag").body());

            assertFalse(lag.get(0).has("aktiv"));
        } finally {
            app.stop();
        }
    }

    @Test
    void ingenLagGirTomListe() throws Exception {
        LokallagRepository ingenLag = () -> List.of();
        Javalin app = startMed(ingenLag);
        try {
            var svar = get(app, "/api/lokallag");

            assertEquals(200, svar.statusCode());
            assertEquals("[]", svar.body());
        } finally {
            app.stop();
        }
    }
}

