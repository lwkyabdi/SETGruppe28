package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.epost.KonsollEpostTjeneste;
import no.hiof.gruppe28.model.AdminPamelding;
import no.hiof.gruppe28.model.Administrator;
import no.hiof.gruppe28.repository.AdminPameldingRepository;
import no.hiof.gruppe28.repository.AdministratorRepository;
import no.hiof.gruppe28.repository.DemoArtikkelRepository;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoKursRepository;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.repository.DemoPameldingRepository;
import no.hiof.gruppe28.service.AdminPameldingService;
import no.hiof.gruppe28.service.InnloggingService;
import no.hiof.gruppe28.service.KontaktpersonService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.LokallagService;
import no.hiof.gruppe28.service.NyhetService;
import no.hiof.gruppe28.service.PameldingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPameldingApiTest {

    private static final String PASSORD = "hemmelig123";

    private final ObjectMapper json = new ObjectMapper();
    private Javalin app;
    private FalskAdminPameldingRepository repo;

    @BeforeEach
    void start() {
        String hash = BCrypt.hashpw(PASSORD, BCrypt.gensalt(4));
        List<Administrator> brukere = List.of(
            new Administrator(1, "Admin", "admin@test.no", hash, "ADMIN", null, true),
            new Administrator(2, "Lag 1", "lag1@test.no", hash, "LOKALLAG", 1L, true),
            new Administrator(3, "Lag 2", "lag2@test.no", hash, "LOKALLAG", 2L, true));
        AdministratorRepository adminRepo = epost ->
            brukere.stream().filter(b -> b.epost().equals(epost)).findFirst();

        repo = new FalskAdminPameldingRepository();
        var kursRepo = new DemoKursRepository(LocalDate.now());
        var pamelding = new PameldingService(
            new DemoPameldingRepository(kursRepo), Clock.systemUTC(), new KonsollEpostTjeneste());

        app = Application.create(
            new KursService(kursRepo, Clock.systemUTC()),
            pamelding,
            new NyhetService(new DemoArtikkelRepository()),
            new InnloggingService(adminRepo),
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(new DemoKontaktpersonRepository()),
            new AdminPameldingService(repo)
        ).start(0);
    }

    @AfterEach
    void stopp() {
        app.stop();
    }

    // Ny nettleser med egen sesjon, innlogget som brukeren med denne e-posten
    private HttpClient loggInn(String epost) throws Exception {
        HttpClient klient = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        var svar = send(klient, "POST", "/api/login",
            "{\"epost\": \"" + epost + "\", \"passord\": \"" + PASSORD + "\"}");
        assertEquals(200, svar.statusCode());
        return klient;
    }

    private HttpResponse<String> send(HttpClient klient, String metode, String sti, String body) throws Exception {
        var bygger = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + sti))
            .method(metode, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            bygger.header("Content-Type", "application/json");
        }
        return klient.send(bygger.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void utenInnloggingGir401() throws Exception {
        var svar = send(HttpClient.newHttpClient(), "GET", "/api/admin/pameldinger", null);

        assertEquals(401, svar.statusCode());
    }

    @Test
    void lagSerBareEgnePameldingerOgPameldteForst() throws Exception {
        JsonNode liste = json.readTree(send(loggInn("lag1@test.no"), "GET", "/api/admin/pameldinger", null).body());

        assertEquals(2, liste.size());
        assertEquals(10, liste.get(0).get("kursId").asLong());
        assertEquals(10, liste.get(1).get("kursId").asLong());
        assertEquals("pameldt", liste.get(0).get("status").asText());
        assertEquals("venteliste", liste.get(1).get("status").asText());
    }

    @Test
    void adminSerAlle() throws Exception {
        JsonNode liste = json.readTree(send(loggInn("admin@test.no"), "GET", "/api/admin/pameldinger", null).body());

        assertEquals(4, liste.size());
    }

    @Test
    void oversiktTellerKursPameldteOgVenteliste() throws Exception {
        JsonNode oversikt = json.readTree(send(loggInn("admin@test.no"), "GET", "/api/admin/oversikt", null).body());

        assertEquals(2, oversikt.get("antallKurs").asInt());
        assertEquals(2, oversikt.get("antallPameldte").asInt());
        assertEquals(2, oversikt.get("antallVenteliste").asInt());
    }

    @Test
    void giPlassGjorVentelisteTilPameldt() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/pameldinger/lag1-venter/gi-plass", null);

        assertEquals(200, svar.statusCode());
        assertEquals("PAMELDT", repo.finn("lag1-venter").orElseThrow().status());
    }

    @Test
    void giPlassNaarKursetErFulltGir409() throws Exception {
        repo.ledigePlasser = 0;

        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/pameldinger/lag1-venter/gi-plass", null);

        assertEquals(409, svar.statusCode());
        assertEquals("VENTELISTE", repo.finn("lag1-venter").orElseThrow().status());
    }

    @Test
    void lagKanIkkeGiPlassHosAnnetLag() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/pameldinger/lag2-venter/gi-plass", null);

        assertEquals(404, svar.statusCode());
        assertEquals("VENTELISTE", repo.finn("lag2-venter").orElseThrow().status());
    }

    @Test
    void lagKanSletteEgenPamelding() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "DELETE", "/api/admin/pameldinger/lag1-pameldt", null);

        assertEquals(200, svar.statusCode());
        assertTrue(repo.finn("lag1-pameldt").isEmpty());
    }

    @Test
    void lagKanIkkeSletteHosAnnetLag() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "DELETE", "/api/admin/pameldinger/lag2-pameldt", null);

        assertEquals(404, svar.statusCode());
        assertTrue(repo.finn("lag2-pameldt").isPresent());
    }

    @Test
    void lagKanIkkeMeldePaAndreLagsKurs() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/kurs/20/pameldinger", "{}");

        assertEquals(404, svar.statusCode());
    }

    @Test
    void ugyldigKursIdGir400() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "GET", "/api/admin/pameldinger?kurs=abc", null);

        assertEquals(400, svar.statusCode());
    }

    // test repository med fire påmeldinger: kurs 10 hører til lag 1, kurs 20 til lag 2
    static class FalskAdminPameldingRepository implements AdminPameldingRepository {

        final List<AdminPamelding> rader = new ArrayList<>(List.of(
            rad("lag1-venter", 10, 1L, "VENTELISTE", 2),
            rad("lag1-pameldt", 10, 1L, "PAMELDT", 1),
            rad("lag2-pameldt", 20, 2L, "PAMELDT", 3),
            rad("lag2-venter", 20, 2L, "VENTELISTE", 4)));
        int ledigePlasser = 1;

        static AdminPamelding rad(String referanse, long kursId, Long lag, String status, int minutt) {
            return new AdminPamelding(referanse, kursId, "Kurs " + kursId, lag, "Kari", referanse,
                referanse + "@test.no", "90012345", true, status,
                LocalDateTime.of(2026, 11, 1, 12, minutt));
        }

        public List<AdminPamelding> finnPameldinger(Long lokallagId, Long kursId) {
            return rader.stream()
                .filter(p -> lokallagId == null || lokallagId.equals(p.kursLokallagId()))
                .filter(p -> kursId == null || p.kursId() == kursId)
                .toList();
        }

        public Optional<AdminPamelding> finn(String referanse) {
            return rader.stream().filter(p -> p.referanse().equals(referanse)).findFirst();
        }

        public int antallKurs(Long lokallagId) {
            return lokallagId == null ? 2 : 1;
        }

        public Optional<Long> finnLokallagForKurs(long kursId) {
            if (kursId == 10) return Optional.of(1L);
            if (kursId == 20) return Optional.of(2L);
            return Optional.empty();
        }

        public boolean giPlass(String referanse) {
            if (ledigePlasser == 0) return false;
            AdminPamelding p = finn(referanse).orElseThrow();
            rader.set(rader.indexOf(p), new AdminPamelding(p.referanse(), p.kursId(), p.kursTittel(),
                p.kursLokallagId(), p.fornavn(), p.etternavn(), p.epost(), p.telefon(),
                p.medlem(), "PAMELDT", p.opprettet()));
            ledigePlasser--;
            return true;
        }

        public void slett(String referanse) {
            rader.removeIf(p -> p.referanse().equals(referanse));
        }
    }
}
