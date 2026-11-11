package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.AdminArtikkel;
import no.hiof.gruppe28.model.Administrator;
import no.hiof.gruppe28.model.ArtikkelData;
import no.hiof.gruppe28.repository.AdminArtikkelRepository;
import no.hiof.gruppe28.repository.AdministratorRepository;
import no.hiof.gruppe28.repository.DemoArtikkelRepository;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.service.AdminNyhetService;
import no.hiof.gruppe28.service.InnloggingService;
import no.hiof.gruppe28.service.KontaktpersonService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.LokallagService;
import no.hiof.gruppe28.service.NyhetService;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.hiof.gruppe28.TestData.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminNyhetApiTest {

    private static final String PASSORD = "hemmelig123";
    private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");
    private static final LocalDateTime NA = LocalDateTime.of(2026, 11, 11, 12, 0);

    private final ObjectMapper json = new ObjectMapper();
    private Javalin app;
    private FalskAdminArtikkelRepository repo;

    @BeforeEach
    void start() {
        String hash = BCrypt.hashpw(PASSORD, BCrypt.gensalt(4));
        List<Administrator> brukere = List.of(
            new Administrator(1, "Admin", "admin@test.no", hash, "ADMIN", null, true),
            new Administrator(2, "Lag 1", "lag1@test.no", hash, "LOKALLAG", 1L, true),
            new Administrator(3, "Lag 2", "lag2@test.no", hash, "LOKALLAG", 2L, true));
        AdministratorRepository adminRepo = epost ->
            brukere.stream().filter(b -> b.epost().equals(epost)).findFirst();

        repo = new FalskAdminArtikkelRepository();
        Clock fastTid = Clock.fixed(NA.atZone(OSLO).toInstant(), OSLO);

        app = Application.create(
            new KursService(repository(List.of()), Clock.systemUTC()),
            null,
            new NyhetService(new DemoArtikkelRepository()),
            new InnloggingService(adminRepo),
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(new DemoKontaktpersonRepository()),
            null,
            null,
            new AdminNyhetService(repo, fastTid),
            null
        ).start(0);
    }

    @AfterEach
    void stopp() {
        app.stop();
    }

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

    // Et gyldig skjema. Testene endrer ett felt om gangen.
    private Map<String, Object> skjema() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("tittel", "Nytt vevkurs");
        s.put("ingress", "Kort tekst.");
        s.put("innhold", "Første avsnitt.\n\nAndre avsnitt.");
        s.put("status", "PUBLISERT");
        return s;
    }

    private long opprett(HttpClient klient, Map<String, Object> s) throws Exception {
        var svar = send(klient, "POST", "/api/admin/nyheter", json.writeValueAsString(s));
        assertEquals(201, svar.statusCode());
        return json.readTree(svar.body()).get("id").asLong();
    }

    @Test
    void lagSerBareEgneNyheter() throws Exception {
        JsonNode liste = json.readTree(send(loggInn("lag1@test.no"), "GET", "/api/admin/nyheter", null).body());

        assertEquals(1, liste.size());
        assertEquals(1, liste.get(0).get("id").asLong());
    }

    @Test
    void adminSerAlleOgsaFylkesnyheter() throws Exception {
        JsonNode liste = json.readTree(send(loggInn("admin@test.no"), "GET", "/api/admin/nyheter", null).body());

        assertEquals(3, liste.size());
    }

    @Test
    void lagKanIkkeHenteFylkesnyhet() throws Exception {
        assertEquals(404, send(loggInn("lag1@test.no"), "GET", "/api/admin/nyheter/3", null).statusCode());
    }

    @Test
    void kladdUtenDatoFarIngenPubliseringstid() throws Exception {
        var s = skjema();
        s.put("status", "KLADD");

        long id = opprett(loggInn("lag1@test.no"), s);

        assertNull(repo.finn(id).orElseThrow().publisertTid());
    }

    @Test
    void publisertUtenDatoFarTidenNa() throws Exception {
        long id = opprett(loggInn("lag1@test.no"), skjema());

        assertEquals(NA, repo.finn(id).orElseThrow().publisertTid());
        assertEquals(1L, repo.finn(id).orElseThrow().lokallagId());
    }

    @Test
    void publisertMedDatoFarDenDatoen() throws Exception {
        var s = skjema();
        s.put("dato", "2026-11-20");

        long id = opprett(loggInn("lag1@test.no"), s);

        assertEquals(LocalDateTime.of(2026, 11, 20, 0, 0), repo.finn(id).orElseThrow().publisertTid());
    }

    @Test
    void lagKanIkkeSkriveForAnnetLag() throws Exception {
        var s = skjema();
        s.put("lokallagId", 2);

        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/nyheter", json.writeValueAsString(s));

        assertEquals(400, svar.statusCode());
    }

    @Test
    void manglendeInnholdGir400() throws Exception {
        var s = skjema();
        s.remove("innhold");

        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/nyheter", json.writeValueAsString(s));

        assertEquals(400, svar.statusCode());
        assertEquals("Innhold må fylles ut.", json.readTree(svar.body()).get("melding").asText());
    }

    @Test
    void adminKanLageFylkesnyhet() throws Exception {
        long id = opprett(loggInn("admin@test.no"), skjema());

        assertNull(repo.finn(id).orElseThrow().lokallagId());
    }

    @Test
    void lagKanIkkeSletteAnnetLagsNyhet() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "DELETE", "/api/admin/nyheter/2", null);

        assertEquals(404, svar.statusCode());
        assertTrue(repo.finn(2).isPresent());
    }

    @Test
    void lagKanSletteEgenNyhet() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "DELETE", "/api/admin/nyheter/1", null);

        assertEquals(200, svar.statusCode());
        assertTrue(repo.finn(1).isEmpty());
    }

    // Falskt repository: nyhet 1 hører til lag 1, nyhet 2 til lag 2, nyhet 3 til hele fylket
    static class FalskAdminArtikkelRepository implements AdminArtikkelRepository {

        final Map<Long, AdminArtikkel> artikler = new HashMap<>();
        long nesteId = 100;

        FalskAdminArtikkelRepository() {
            artikler.put(1L, artikkel(1, 1L));
            artikler.put(2L, artikkel(2, 2L));
            artikler.put(3L, artikkel(3, null));
        }

        static AdminArtikkel artikkel(long id, Long lag) {
            return new AdminArtikkel(id, "Nyhet " + id, "Ingress", "Innhold", null, "",
                lag, lag == null ? null : "Lag " + lag, "PUBLISERT",
                LocalDateTime.of(2026, 11, (int) id, 10, 0));
        }

        static AdminArtikkel fra(long id, ArtikkelData d) {
            return new AdminArtikkel(id, d.tittel(), d.ingress(), d.innhold(), d.bildesti(), d.bildealt(),
                d.lokallagId(), d.lokallagId() == null ? null : "Lag " + d.lokallagId(),
                d.status(), d.publisertTid());
        }

        public List<AdminArtikkel> finnAlle(Long lokallagId) {
            return artikler.values().stream()
                .filter(a -> lokallagId == null || lokallagId.equals(a.lokallagId()))
                .toList();
        }

        public Optional<AdminArtikkel> finn(long id) {
            return Optional.ofNullable(artikler.get(id));
        }

        public boolean lokallagFinnes(long lokallagId) {
            return lokallagId == 1 || lokallagId == 2;
        }

        public long opprett(ArtikkelData data) {
            long id = nesteId++;
            artikler.put(id, fra(id, data));
            return id;
        }

        public boolean oppdater(long id, ArtikkelData data) {
            if (!artikler.containsKey(id)) return false;
            artikler.put(id, fra(id, data));
            return true;
        }

        public boolean slett(long id) {
            return artikler.remove(id) != null;
        }
    }
}
