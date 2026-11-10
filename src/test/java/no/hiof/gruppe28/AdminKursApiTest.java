package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.model.AdminKurs;
import no.hiof.gruppe28.model.Administrator;
import no.hiof.gruppe28.model.KursData;
import no.hiof.gruppe28.repository.AdminKursRepository;
import no.hiof.gruppe28.repository.AdministratorRepository;
import no.hiof.gruppe28.repository.DemoArtikkelRepository;
import no.hiof.gruppe28.repository.DemoKontaktpersonRepository;
import no.hiof.gruppe28.repository.DemoLokallagRepository;
import no.hiof.gruppe28.service.AdminKursService;
import no.hiof.gruppe28.service.InnloggingService;
import no.hiof.gruppe28.service.KontaktpersonService;
import no.hiof.gruppe28.service.KursService;
import no.hiof.gruppe28.service.LokallagService;
import no.hiof.gruppe28.service.NyhetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static no.hiof.gruppe28.TestData.repository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminKursApiTest {

    private static final String PASSORD = "hemmelig123";

    private final ObjectMapper json = new ObjectMapper();
    private Javalin app;
    private FalskAdminKursRepository repo;

    @BeforeEach
    void start() {
        String hash = BCrypt.hashpw(PASSORD, BCrypt.gensalt(4));
        List<Administrator> brukere = List.of(
            new Administrator(1, "Admin", "admin@test.no", hash, "ADMIN", null, true),
            new Administrator(2, "Lag 1", "lag1@test.no", hash, "LOKALLAG", 1L, true),
            new Administrator(3, "Lag 2", "lag2@test.no", hash, "LOKALLAG", 2L, true));
        AdministratorRepository adminRepo = epost ->
            brukere.stream().filter(b -> b.epost().equals(epost)).findFirst();

        repo = new FalskAdminKursRepository();

        app = Application.create(
            new KursService(repository(List.of()), Clock.systemUTC()),
            null,
            new NyhetService(new DemoArtikkelRepository()),
            new InnloggingService(adminRepo),
            new LokallagService(new DemoLokallagRepository()),
            new KontaktpersonService(new DemoKontaktpersonRepository()),
            null,
            new AdminKursService(repo)
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

    // Et gyldig skjema med alle feltene. Testene endrer ett felt om gangen.
    private Map<String, Object> skjema() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("tittel", "Strikkekurs");
        s.put("beskrivelse", "Lær å strikke.");
        s.put("dato", "2026-12-01");
        s.put("tidspunkt", "18:00–21:00");
        s.put("varighet", "4 kvelder");
        s.put("sted", "Husflidshuset");
        s.put("tema", "Strikking");
        s.put("malgruppe", "Voksne");
        s.put("niva", "Nybegynner");
        s.put("utstyr", "Pinner nr. 4");
        s.put("medlemspris", 900);
        s.put("ordinaerpris", 1200);
        s.put("kapasitet", 10);
        s.put("status", "PUBLISERT");
        s.put("kontaktId", 1);
        return s;
    }

    private String somJson(Map<String, Object> s) throws Exception {
        return json.writeValueAsString(s);
    }

    @Test
    void lagSerBareEgneKursOgsaKladder() throws Exception {
        JsonNode kurs = json.readTree(send(loggInn("lag1@test.no"), "GET", "/api/admin/kurs", null).body());

        assertEquals(2, kurs.size());
        assertEquals(10, kurs.get(0).get("id").asLong());
        assertEquals("KLADD", kurs.get(1).get("status").asText());
    }

    @Test
    void adminSerAlleKurs() throws Exception {
        JsonNode kurs = json.readTree(send(loggInn("admin@test.no"), "GET", "/api/admin/kurs", null).body());

        assertEquals(3, kurs.size());
    }

    @Test
    void lagKanIkkeHenteAnnetLagsKurs() throws Exception {
        assertEquals(404, send(loggInn("lag1@test.no"), "GET", "/api/admin/kurs/20", null).statusCode());
    }

    @Test
    void opprettGir201MedEgetLagOgAlleLedigePlasser() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/kurs", somJson(skjema()));
        long nyId = json.readTree(svar.body()).get("id").asLong();

        assertEquals(201, svar.statusCode());
        AdminKurs lagret = repo.finn(nyId).orElseThrow();
        assertEquals(1L, lagret.lokallagId());
        assertEquals(10, lagret.ledigePlasser());
    }

    @Test
    void lagKanIkkeOppretteKursForAnnetLag() throws Exception {
        var s = skjema();
        s.put("lokallagId", 2);

        assertEquals(400, send(loggInn("lag1@test.no"), "POST", "/api/admin/kurs", somJson(s)).statusCode());
    }

    @Test
    void manglendeFeltGir400MedNavnetPaFeltet() throws Exception {
        var s = skjema();
        s.remove("beskrivelse");

        var svar = send(loggInn("lag1@test.no"), "POST", "/api/admin/kurs", somJson(s));

        assertEquals(400, svar.statusCode());
        assertEquals("Beskrivelse må fylles ut.", json.readTree(svar.body()).get("melding").asText());
    }

    @Test
    void feilDatoGir400() throws Exception {
        var s = skjema();
        s.put("dato", "1. desember");

        assertEquals(400, send(loggInn("lag1@test.no"), "POST", "/api/admin/kurs", somJson(s)).statusCode());
    }

    @Test
    void ukjentKontaktpersonGir400() throws Exception {
        var s = skjema();
        s.put("kontaktId", 99);

        assertEquals(400, send(loggInn("lag1@test.no"), "POST", "/api/admin/kurs", somJson(s)).statusCode());
    }

    @Test
    void kapasitetLavereEnnPameldteGir409() throws Exception {
        // Kurs 10 har 10 plasser og 2 ledige, altså 8 påmeldte
        var s = skjema();
        s.put("kapasitet", 5);

        var svar = send(loggInn("lag1@test.no"), "PUT", "/api/admin/kurs/10", somJson(s));

        assertEquals(409, svar.statusCode());
        assertEquals(10, repo.finn(10).orElseThrow().kapasitet());
    }

    @Test
    void lagKanIkkeEndreAnnetLagsKurs() throws Exception {
        assertEquals(404, send(loggInn("lag1@test.no"), "PUT", "/api/admin/kurs/20", somJson(skjema())).statusCode());
    }

    @Test
    void slettMedPameldteGir409() throws Exception {
        var svar = send(loggInn("lag2@test.no"), "DELETE", "/api/admin/kurs/20", null);

        assertEquals(409, svar.statusCode());
        assertTrue(repo.finn(20).isPresent());
    }

    @Test
    void slettUtenPameldteFjernerKurset() throws Exception {
        var svar = send(loggInn("lag1@test.no"), "DELETE", "/api/admin/kurs/11", null);

        assertEquals(200, svar.statusCode());
        assertTrue(repo.finn(11).isEmpty());
    }

    // Falskt repository: kurs 10 og 11 hører til lag 1, kurs 20 til lag 2 (har påmeldte)
    static class FalskAdminKursRepository implements AdminKursRepository {

        final Map<Long, AdminKurs> kurs = new HashMap<>();
        final Set<Long> harPameldte = new HashSet<>(Set.of(20L));
        long nesteId = 100;

        FalskAdminKursRepository() {
            kurs.put(10L, kurs(10, 1L, LocalDate.of(2026, 12, 1), 10, 2, "PUBLISERT"));
            kurs.put(11L, kurs(11, 1L, LocalDate.of(2026, 12, 5), 8, 8, "KLADD"));
            kurs.put(20L, kurs(20, 2L, LocalDate.of(2026, 12, 3), 6, 1, "PUBLISERT"));
        }

        static AdminKurs kurs(long id, Long lag, LocalDate dato, int kapasitet, int ledige, String status) {
            return new AdminKurs(id, "Kurs " + id, "Beskrivelse", dato, "18:00", "1 kveld", "Sted",
                lag, "Lag " + lag, "Strikking", "Voksne", "Nybegynner", "Pinner",
                new BigDecimal("900"), new BigDecimal("1200"), kapasitet, ledige, status, 1, "", "");
        }

        static AdminKurs fra(long id, KursData d, int ledige) {
            return new AdminKurs(id, d.tittel(), d.beskrivelse(), d.dato(), d.tidspunkt(), d.varighet(),
                d.sted(), d.lokallagId(), "Lag " + d.lokallagId(), d.tema(), d.malgruppe(), d.niva(),
                d.utstyr(), d.medlemspris(), d.ordinaerpris(), d.kapasitet(), ledige, d.status(),
                d.kontaktId(), d.bildesti(), d.bildealt());
        }

        public List<AdminKurs> finnAlle(Long lokallagId) {
            return kurs.values().stream()
                .filter(k -> lokallagId == null || lokallagId.equals(k.lokallagId()))
                .toList();
        }

        public Optional<AdminKurs> finn(long id) {
            return Optional.ofNullable(kurs.get(id));
        }

        public boolean lokallagFinnes(long lokallagId) {
            return lokallagId == 1 || lokallagId == 2;
        }

        public boolean kontaktpersonFinnes(long kontaktId) {
            return kontaktId == 1;
        }

        public long opprett(KursData data) {
            long id = nesteId++;
            kurs.put(id, fra(id, data, data.kapasitet()));
            return id;
        }

        public boolean oppdater(long id, KursData data) {
            AdminKurs gammel = kurs.get(id);
            int nyeLedige = gammel.ledigePlasser() + (data.kapasitet() - gammel.kapasitet());
            if (nyeLedige < 0) {
                return false;
            }
            kurs.put(id, fra(id, data, nyeLedige));
            return true;
        }

        public boolean slettHvisIngenPameldte(long id) {
            if (harPameldte.contains(id)) {
                return false;
            }
            kurs.remove(id);
            return true;
        }
    }
}

