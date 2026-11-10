package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.repository.AdministratorRepository;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.repository.DemoNyhetRepository;
import no.hiof.gruppe28.repository.KursRepository;
import no.hiof.gruppe28.service.ApiException;
import no.hiof.gruppe28.service.InnloggingService;
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
import java.util.Optional;

import static no.hiof.gruppe28.TestData.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FeilhandteringApiTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private Javalin startMedFeil(RuntimeException feil) {
        KursRepository feilendeRepository = new KursRepository() {
            public List<Kurs> findPubliserte() { throw feil; }
            public Optional<Kurs> findPublisert(long id) { throw feil; }
        };
        return Application.create(new KursService(feilendeRepository, Clock.systemUTC())).start(0);
    }

    private HttpResponse<String> get(Javalin app, String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void uventetFeilGir500MedMelding() throws Exception {
        Javalin app = startMedFeil(new IllegalStateException("Hemmelig intern detalj"));
        try {
            var svar = get(app, "/api/kurs");

            assertEquals(500, svar.statusCode());
            assertEquals("Noe gikk galt. Prøv igjen senere.", json.readTree(svar.body()).get("melding").asText());
            assertFalse(svar.body().contains("Hemmelig intern detalj"));
        } finally {
            app.stop();
        }
    }

    @Test
    void apiExceptionGirStatusOgMeldingFraFeilen() throws Exception {
        Javalin app = startMedFeil(new ApiException(409, "Konflikt i testen."));
        try {
            var svar = get(app, "/api/kurs");

            assertEquals(409, svar.statusCode());
            assertEquals("Konflikt i testen.", json.readTree(svar.body()).get("melding").asText());
        } finally {
            app.stop();
        }
    }

    @Test
    void adminUtenInnloggingGir401MedMelding() throws Exception {
        AdministratorRepository ingenAdministratorer = epost -> Optional.empty();
        Javalin app = Application.create(
            new KursService(repository(List.of()), Clock.systemUTC()),
            null,
            new NyhetService(new DemoNyhetRepository()),
            new InnloggingService(ingenAdministratorer),
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(new DemoKontaktpersonRepository())
        ).start(0);
        try {
            var svar = get(app, "/api/admin/oversikt");

            assertEquals(401, svar.statusCode());
            assertEquals("Du er ikke logget inn.", json.readTree(svar.body()).get("melding").asText());
        } finally {
            app.stop();
        }
    }
}

