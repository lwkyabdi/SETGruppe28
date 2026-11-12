package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.NyhetSkjema;
import no.hiof.gruppe28.model.ArtikkelData;
import no.hiof.gruppe28.repository.*;
import no.hiof.gruppe28.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mindrot.jbcrypt.BCrypt;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_URL", matches = ".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcAdminArtikkelRepositoryTest {
    private DatabaseConfig database;
    private JdbcAdminArtikkelRepository repository;
    private static final String PASSORD = "ArtikkelTest123";
    private static final String HASH = BCrypt.hashpw(PASSORD, BCrypt.gensalt(4));
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-11T11:00:00Z"), ZoneId.of("Europe/Oslo"));
    private static final LocalDateTime TID = LocalDateTime.of(2026, 11, 11, 12, 0);

    @BeforeAll void schema() throws Exception {
        String url = System.getenv("MYSQL_TEST_URL");
        if (!url.matches("^jdbc:mysql://[^/]+/husflid_test(\\?.*)?$")) {
            throw new IllegalArgumentException("Testene krever den separate databasen husflid_test.");
        }
        database = new DatabaseConfig(url, System.getenv("MYSQL_TEST_USER"), System.getenv("MYSQL_TEST_PASSWORD"));
        try (var c = database.connect(); var s = c.createStatement()) {
            assertEquals("husflid_test", c.getCatalog());
            for (String tabell : new String[]{"venteliste", "pamelding", "aktivitet", "artikkel",
                    "medlemsinteresse", "administrator", "kurs", "kontaktperson", "lokallag", "id_teller", "kontaktmelding"}) {
                s.execute("DROP TABLE IF EXISTS " + tabell);
            }
            for (String fil : new String[]{"01-schema.sql", "02-seed.sql", "03-pamelding.sql",
                    "04-frontend.sql", "05-kurs-lokallag.sql", "06-pamelding-status.sql"}) {
                String sql = Files.readString(Path.of("database", fil), StandardCharsets.UTF_8)
                        .replaceAll("(?m)^\\s*--.*$", "");
                for (String setning : sql.split(";")) if (!setning.isBlank()) s.execute(setning);
            }
        }
        repository = new JdbcAdminArtikkelRepository(database);
    }

    @BeforeEach void data() throws Exception {
        for (String tabell : new String[]{"venteliste", "pamelding", "aktivitet", "artikkel",
                "medlemsinteresse", "administrator", "kurs", "kontaktperson", "lokallag"}) {
            sql("DELETE FROM " + tabell);
        }
        sql("INSERT INTO lokallag (id, slug, navn) VALUES (10, 'fredrikstad', 'Fredrikstad'), (20, 'moss', 'Moss')");
        sql("""
                INSERT INTO artikkel (id, tittel, ingress, innhold, bildesti, bildealt,
                    lokallag_id, status, publisert_tid)
                VALUES (1, 'Publisert Fredrikstad', 'Ingress', 'Tekst med æøå', '/bilder/nyhet-1.jpg',
                        'Bilde', 10, 'PUBLISERT', '2026-11-10 12:00:00'),
                       (2, 'Kladd Fredrikstad', 'Ingress', 'Kladd', NULL, '', 10, 'KLADD', NULL),
                       (3, 'Publisert Moss', 'Ingress', 'Moss', NULL, '', 20, 'PUBLISERT', '2026-11-09 12:00:00'),
                       (4, 'Fellesnyhet', 'Ingress', 'Østfold', NULL, '', NULL, 'PUBLISERT', '2026-11-08 12:00:00')
                """);
        try (var c = database.connect(); var s = c.prepareStatement("""
                INSERT INTO administrator (id, navn, epost, passord_hash, rolle, lokallag_id, aktiv)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """)) {
            for (int i = 1; i <= 3; i++) {
                s.setLong(1, i); s.setString(2, "Testbruker " + i);
                s.setString(3, "artikkel" + i + "@example.test"); s.setString(4, HASH);
                s.setString(5, i == 1 ? "ADMIN" : "LOKALLAG");
                if (i == 1) s.setNull(6, java.sql.Types.BIGINT); else s.setLong(6, i == 2 ? 10 : 20);
                s.executeUpdate();
            }
        }
    }

    @Test void alleTarMedKladderOgFellesnyheter() {
        assertEquals(4, repository.finnAlle(null).size());
        assertEquals(2, repository.finnAlle(10L).size());
        assertEquals(1, repository.finnAlle(20L).size());
        assertEquals(0, repository.finnAlle(999L).size());
    }

    @Test void finnHenterAlleFelterOgLokallagsnavn() {
        var a = repository.finn(1).orElseThrow();
        assertEquals("Publisert Fredrikstad", a.tittel()); assertEquals("Ingress", a.ingress());
        assertEquals("Tekst med æøå", a.innhold()); assertEquals("/bilder/nyhet-1.jpg", a.bildesti());
        assertEquals("Bilde", a.bildealt()); assertEquals(10L, a.lokallagId());
        assertEquals("Fredrikstad", a.lokallagNavn()); assertEquals("PUBLISERT", a.status());
        assertEquals(LocalDateTime.of(2026, 11, 10, 12, 0), a.publisertTid());
    }

    @Test void nullfelterBeholdesForKladdOgFellesnyhet() {
        var kladd = repository.finn(2).orElseThrow();
        assertNull(kladd.publisertTid()); assertNull(kladd.bildesti());
        var felles = repository.finn(4).orElseThrow();
        assertNull(felles.lokallagId()); assertNull(felles.lokallagNavn());
    }

    @Test void ukjenteIdErGirTomtOgLokallagKontrolleres() {
        assertTrue(repository.finn(999).isEmpty());
        assertTrue(repository.lokallagFinnes(10)); assertFalse(repository.lokallagFinnes(999));
    }

    @Test void opprettReturnererGenerertIdMedAlleFeltene() {
        long id = repository.opprett(artikkel("Ny artikkel", 10L, "PUBLISERT", TID));
        assertTrue(id > 4);
        var a = repository.finn(id).orElseThrow();
        assertEquals("Ny artikkel", a.tittel()); assertEquals(TID, a.publisertTid());
        assertEquals("Innhold med æøå", a.innhold()); assertEquals("Fredrikstad", a.lokallagNavn());
    }

    @Test void opprettKladdOgFellesnyhetMedNull() {
        long id = repository.opprett(artikkel("Felles kladd", null, "KLADD", null));
        var a = repository.finn(id).orElseThrow();
        assertNull(a.lokallagId()); assertNull(a.publisertTid());
        assertTrue(new JdbcArtikkelRepository(database).finnPublisert(id).isEmpty());
    }

    @Test void oppdaterEndrerFelteneOgBeholderId() {
        assertTrue(repository.oppdater(1, artikkel("Endret nyhet", 20L, "KLADD", null)));
        var a = repository.finn(1).orElseThrow();
        assertEquals("Endret nyhet", a.tittel()); assertEquals("Moss", a.lokallagNavn());
        assertEquals("KLADD", a.status()); assertNull(a.publisertTid());
        assertTrue(new JdbcArtikkelRepository(database).finnPublisert(1).isEmpty());
    }

    @Test void identiskOppdateringReturnererTrueOgsaaMedAffectedRows() {
        var config = new DatabaseConfig(database.url() + (database.url().contains("?") ? "&" : "?")
                + "useAffectedRows=true", database.username(), database.password());
        var repo = new JdbcAdminArtikkelRepository(config);
        var d = artikkel("Uendret", 10L, "PUBLISERT", TID);
        long id = repo.opprett(d);
        assertTrue(repo.oppdater(id, d)); assertTrue(repo.oppdater(id, d));
    }

    @Test void manglendeOppdateringOgSlettingReturnererFalse() {
        assertFalse(repository.oppdater(999, artikkel("Ukjent", null, "KLADD", null)));
        assertFalse(repository.slett(999)); assertEquals(4, repository.finnAlle(null).size());
    }

    @Test void slettFjernerRadenEnGang() {
        assertTrue(repository.slett(1)); assertFalse(repository.slett(1));
        assertTrue(repository.finn(1).isEmpty());
        assertTrue(new JdbcArtikkelRepository(database).finnPublisert(1).isEmpty());
    }

    @Test void feilFremmednokkelGirIngenNyRad() {
        assertThrows(DatabaseException.class,
                () -> repository.opprett(artikkel("Feil lag", 999L, "PUBLISERT", TID)));
        assertEquals(4, repository.finnAlle(null).size());
    }

    @Test void ugyldigOppdateringRullesTilbake() {
        var forrige = repository.finn(1).orElseThrow();
        assertThrows(DatabaseException.class,
                () -> repository.oppdater(1, artikkel("Skal ikke lagres", 10L, "PUBLISERT", null)));
        assertEquals(forrige, repository.finn(1).orElseThrow());
    }

    @Test void publiseringFraServiceBlirSynligIOffentligRepository() {
        var service = new AdminNyhetService(repository, CLOCK);
        service.oppdater(admin(), 2, skjema("Publisert nå", null, "PUBLISERT"));
        var a = new JdbcArtikkelRepository(database).finnPublisert(2).orElseThrow();
        assertEquals(TID, a.publisertTid()); assertEquals("Publisert nå", a.tittel());
    }

    @Test void lokallagskontoKanIkkeEndreAndreLagEllerFellesnyheter() {
        var service = new AdminNyhetService(repository, CLOCK);
        var bruker = new InnloggetBruker(2, "Lag", "lag@example.test", "LOKALLAG", 10L);
        assertEquals(404, assertThrows(ApiException.class, () -> service.finn(bruker, 3)).status());
        assertEquals(404, assertThrows(ApiException.class, () -> service.slett(bruker, 4)).status());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.opprett(bruker, skjema("Feil lag", 20L, "KLADD"))).status());
        assertEquals(4, repository.finnAlle(null).size());
    }

    @Test void tekstgrenserSamsvarerMedDatabasen() throws Exception {
        var service = new AdminNyhetService(repository, CLOCK);
        var json = new ObjectMapper();
        var original = (com.fasterxml.jackson.databind.node.ObjectNode) json.valueToTree(skjema("Gyldig", null, "KLADD"));
        for (var grense : Map.of("tittel", 180, "bildesti", 255, "bildealt", 180).entrySet()) {
            var node = original.deepCopy().put(grense.getKey(), "x".repeat(grense.getValue() + 1));
            var s = json.treeToValue(node, NyhetSkjema.class);
            assertEquals(400, assertThrows(ApiException.class, () -> service.opprett(admin(), s)).status());
            original.put(grense.getKey(), "x".repeat(grense.getValue()));
        }
        long id = service.opprett(admin(), json.treeToValue(original, NyhetSkjema.class));
        assertTrue(repository.finn(id).isPresent());
    }

    @Test void nyhetsApiKjorerMedJdbcInnloggingOgTilgangskontroll() throws Exception {
        var app = app().start(0);
        String adresse = "http://localhost:" + app.port();
        var json = new ObjectMapper();
        var global = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        var lokalt = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        try {
            assertEquals(401, send(global, adresse, "GET", "/api/admin/nyheter", null).statusCode());
            assertEquals(200, send(global, adresse, "POST", "/api/login",
                    json.writeValueAsString(Map.of("epost", "artikkel1@example.test", "passord", PASSORD))).statusCode());
            assertEquals(200, send(lokalt, adresse, "POST", "/api/login",
                    json.writeValueAsString(Map.of("epost", "artikkel2@example.test", "passord", PASSORD))).statusCode());
            assertEquals(4, json.readTree(send(global, adresse, "GET", "/api/admin/nyheter", null).body()).size());
            assertEquals(2, json.readTree(send(lokalt, adresse, "GET", "/api/admin/nyheter", null).body()).size());
            assertEquals(404, send(lokalt, adresse, "DELETE", "/api/admin/nyheter/3", null).statusCode());
            assertEquals(404, send(lokalt, adresse, "GET", "/api/admin/nyheter/4", null).statusCode());
            var opprettet = send(global, adresse, "POST", "/api/admin/nyheter",
                    json.writeValueAsString(skjema("Ny fra JDBC API", null, "KLADD")));
            assertEquals(201, opprettet.statusCode());
            long id = json.readTree(opprettet.body()).get("id").asLong();
            assertEquals(404, send(global, adresse, "GET", "/api/nyheter/" + id, null).statusCode());
            assertEquals(200, send(global, adresse, "PUT", "/api/admin/nyheter/" + id,
                    json.writeValueAsString(skjema("Publisert fra API", null, "PUBLISERT"))).statusCode());
            var offentlig = send(global, adresse, "GET", "/api/nyheter/" + id, null);
            assertEquals(200, offentlig.statusCode());
            assertEquals("Østfold Husflidslag", json.readTree(offentlig.body()).get("lokallag").asText());
            assertTrue(offentlig.headers().firstValue("Content-Type").orElseThrow().contains("charset=utf-8"));
            assertEquals(200, send(global, adresse, "DELETE", "/api/admin/nyheter/" + id, null).statusCode());
            assertEquals(404, send(global, adresse, "DELETE", "/api/admin/nyheter/" + id, null).statusCode());
        } finally { app.stop(); }
    }

    private Javalin app() {
        return Application.create(new KursService(new JdbcKursRepository(database), CLOCK),
                new PameldingService(new JdbcPameldingRepository(database), CLOCK, (e, k) -> { }),
                new NyhetService(new JdbcArtikkelRepository(database)),
                new InnloggingService(new JdbcAdministratorRepository(database)),
                new LokallagService(new JdbcLokallagRepository(database)),
                new KontaktpersonService(new JdbcKontaktpersonRepository(database)),
                new AdminPameldingService(new JdbcAdminPameldingRepository(database)),
                new AdminKursService(new JdbcAdminKursRepository(database)),
                new AdminNyhetService(repository, CLOCK), null);
    }

    private static ArtikkelData artikkel(String tittel, Long lag, String status, LocalDateTime tid) {
        return new ArtikkelData(tittel, "Ingress", "Innhold med æøå", "/bilder/nyhet-1.jpg", "Illustrasjon", lag, status, tid);
    }

    private static NyhetSkjema skjema(String tittel, Long lag, String status) {
        return new NyhetSkjema(tittel, "Ingress", "Innhold med æøå", "", "", lag, status, null);
    }

    private static InnloggetBruker admin() {
        return new InnloggetBruker(1, "Admin", "admin@example.test", "ADMIN", null);
    }

    private void sql(String sql) throws Exception {
        try (var c = database.connect(); var s = c.createStatement()) { s.execute(sql); }
    }

    private static HttpResponse<String> send(HttpClient klient, String adresse, String metode, String sti, String body)
            throws Exception {
        var req = HttpRequest.newBuilder(URI.create(adresse + sti))
                .method(metode, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) req.header("Content-Type", "application/json");
        return klient.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }
}
