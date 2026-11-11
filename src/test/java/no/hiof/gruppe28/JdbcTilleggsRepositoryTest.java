package no.hiof.gruppe28;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.dto.PameldingRequest;
import no.hiof.gruppe28.dto.KursSkjema;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.model.KursData;
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
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_URL", matches = ".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcTilleggsRepositoryTest {

    private DatabaseConfig database;
    private JdbcAdminPameldingRepository admin;
    private static final String PASSORD = "BareTilLokaleTester123";
    private static final String HASH = BCrypt.hashpw(PASSORD, BCrypt.gensalt(4));

    @BeforeAll
    void opprettTestdatabase() throws Exception {
        String url = System.getenv("MYSQL_TEST_URL");
        if (!url.matches("^jdbc:mysql://[^/]+/husflid_test(\\?.*)?$")) {
            throw new IllegalArgumentException("Testene krever den separate databasen husflid_test.");
        }
        database = new DatabaseConfig(url,
                System.getenv("MYSQL_TEST_USER"), System.getenv("MYSQL_TEST_PASSWORD"));
        try (var c = database.connect(); var s = c.createStatement()) {
            assertEquals("husflid_test", c.getCatalog());
            for (String tabell : new String[]{"venteliste", "pamelding", "aktivitet", "artikkel",
                    "medlemsinteresse", "administrator", "kurs", "kontaktperson", "lokallag",
                    "id_teller", "kontaktmelding"}) {
                s.execute("DROP TABLE IF EXISTS " + tabell);
            }
            for (String fil : new String[]{"01-schema.sql", "02-seed.sql", "03-pamelding.sql",
                    "04-frontend.sql", "05-kurs-lokallag.sql", "06-pamelding-status.sql"}) {
                String sql = Files.readString(Path.of("database", fil), StandardCharsets.UTF_8)
                        .replaceAll("(?m)^\\s*--.*$", "");
                for (String setning : sql.split(";")) {
                    if (!setning.isBlank()) s.execute(setning);
                }
            }
        }
        admin = new JdbcAdminPameldingRepository(database);
    }

    @BeforeEach
    void testdata() throws Exception {
        sql("DROP TRIGGER IF EXISTS jdbc_test_avvis_pamelding");
        sql("DROP TRIGGER IF EXISTS jdbc_test_avvis_plasstall");
        for (String tabell : new String[]{"venteliste", "pamelding", "aktivitet", "artikkel",
                "medlemsinteresse", "administrator", "kurs", "kontaktperson", "lokallag"}) {
            sql("DELETE FROM " + tabell);
        }
        sql("UPDATE id_teller SET siste_id = 0 WHERE navn = 'kurs'");
        sql("""
                INSERT INTO lokallag (id, slug, navn, beskrivelse, adresse, epost, telefon, bildesti, aktiv)
                VALUES (10, 'fredrikstad', 'Fredrikstad', 'Testlag', 'Testveien 1',
                        'lag@example.test', '99999999', '/bilder/hflogo.svg', 1),
                       (20, 'moss', 'Moss', NULL, NULL, NULL, NULL, NULL, 1),
                       (30, 'inaktiv', 'Inaktivt lag', NULL, NULL, NULL, NULL, NULL, 0)
                """);
        sql("""
                INSERT INTO kontaktperson (id, navn, epost, telefon, bilde, verv, sted, vis_pa_kontaktside)
                VALUES (1, 'Anna Test', 'anna@example.test', '99999999', '/bilder/hflogo.svg',
                        'Kursansvarlig', 'Fredrikstad', 1),
                       (2, 'Skjult Test', 'skjult@example.test', '99999999', '', NULL, NULL, 0),
                       (3, 'Bjarne Test', 'bjarne@example.test', '99999999', '', NULL, NULL, 1)
                """);
        sql("""
                INSERT INTO kurs (id, tittel, beskrivelse, dato, tidspunkt, varighet, sted,
                    lokallag, tema, malgruppe, niva, utstyr, medlemspris, ordinaerpris,
                    kapasitet, ledige_plasser, status, kontakt_id, lokallag_id)
                VALUES (501, 'Kurs Fredrikstad', 'Test', CURRENT_DATE + INTERVAL 30 DAY,
                        '10:00–15:00', '1 dag', 'Testlokale', 'Gammelt navn', 'Søm',
                        'Voksne', 'Nybegynner', '', 900, 1200, 3, 1, 'PUBLISERT', 1, 10),
                       (502, 'Fullt kurs Moss', 'Test', CURRENT_DATE + INTERVAL 30 DAY,
                        '10:00–15:00', '1 dag', 'Testlokale', 'Moss', 'Veving',
                        'Voksne', 'Nybegynner', '', 900, 1200, 2, 0, 'PUBLISERT', 2, 20),
                       (503, 'Kladd Fredrikstad', 'Test', NULL, '', '', '', 'Fredrikstad', '',
                        '', '', '', 0, 0, 2, 2, 'KLADD', 1, 10),
                       (504, 'Kurs uten lag', 'Test', NULL, '', '', '', 'Ukjent', '',
                        '', '', '', 0, 0, 2, 2, 'KLADD', 1, NULL)
                """);
        sql("""
                INSERT INTO artikkel (id, tittel, ingress, innhold, bildesti, bildealt,
                        lokallag_id, status, publisert_tid)
                VALUES (1, 'Nyhet Fredrikstad', 'Ingress', 'Tekst med æøå', '/bilder/nyhet-1.jpg',
                        'Kursbilde', 10, 'PUBLISERT', '2026-11-01 10:00:00'),
                       (2, 'Nyhet Østfold', 'Ingress', 'Fellesnyhet', NULL, '', NULL,
                        'PUBLISERT', '2026-11-01 10:00:00'),
                       (3, 'Kladd', 'Ingress', 'Skal skjules', NULL, '', 10, 'KLADD', NULL),
                       (4, 'Eldre nyhet Moss', 'Ingress', 'Eldre tekst', NULL, '', 20,
                        'PUBLISERT', '2026-10-01 10:00:00')
                """);
        try (var c = database.connect(); var s = c.prepareStatement("""
                INSERT INTO administrator (id, navn, epost, passord_hash, rolle, lokallag_id, aktiv)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """)) {
            for (int i = 1; i <= 4; i++) {
                s.setLong(1, i); s.setString(2, "Testbruker " + i);
                s.setString(3, "bruker" + i + "@example.test"); s.setString(4, HASH);
                s.setString(5, i == 1 ? "ADMIN" : "LOKALLAG");
                if (i == 1) s.setNull(6, java.sql.Types.BIGINT);
                else s.setLong(6, i == 3 ? 20 : 10);
                s.setBoolean(7, i != 4); s.executeUpdate();
            }
        }
        pamelding("pameldt-501", 501, "PAMELDT", "2026-11-01 09:00:00");
        pamelding("venter-501-a", 501, "VENTELISTE", "2026-11-01 10:00:00");
        pamelding("venter-501-b", 501, "VENTELISTE", "2026-11-01 11:00:00");
        pamelding("pameldt-502", 502, "PAMELDT", "2026-11-01 09:00:00");
        pamelding("venter-502", 502, "VENTELISTE", "2026-11-01 10:00:00");
    }

    @Test void administratorHarRiktigRolleLagOgHash() {
        var repo = new JdbcAdministratorRepository(database);
        var global = repo.finnAktivMedEpost(" bruker1@example.test ").orElseThrow();
        assertNull(global.lokallagId());
        assertEquals("ADMIN", global.rolle());
        assertEquals(HASH, global.passordHash());
        assertEquals(10L, repo.finnAktivMedEpost("bruker2@example.test").orElseThrow().lokallagId());
    }

    @Test void inaktiveUkjenteOgSqlTekstGirIngenAdministrator() {
        var repo = new JdbcAdministratorRepository(database);
        assertTrue(repo.finnAktivMedEpost("bruker4@example.test").isEmpty());
        assertTrue(repo.finnAktivMedEpost("ukjent@example.test").isEmpty());
        assertTrue(repo.finnAktivMedEpost("' OR 1=1 --").isEmpty());
        assertTrue(repo.finnAktivMedEpost(null).isEmpty());
    }

    @Test void innloggingKontrollererHashFraDatabasen() {
        var service = new InnloggingService(new JdbcAdministratorRepository(database));
        assertTrue(service.loggInn("bruker1@example.test", PASSORD).isPresent());
        assertTrue(service.loggInn("bruker1@example.test", "feil").isEmpty());
        assertTrue(service.loggInn("bruker4@example.test", PASSORD).isEmpty());
    }

    @Test void lokallagFiltreresSorteresOgBeholderNullfelt() {
        var lag = new JdbcLokallagRepository(database).finnAktive();
        assertEquals(List.of("Fredrikstad", "Moss"), lag.stream().map(l -> l.navn()).toList());
        assertEquals("fredrikstad", lag.get(0).slug());
        assertEquals("Testveien 1", lag.get(0).adresse());
        assertEquals("lag@example.test", lag.get(0).epost());
        assertNull(lag.get(1).bildesti());
    }

    @Test void bareValgteKontakterVisesMedVervOgSted() {
        var personer = new JdbcKontaktpersonRepository(database).finnForKontaktside();
        assertEquals(List.of("Anna Test", "Bjarne Test"), personer.stream().map(p -> p.navn()).toList());
        assertEquals("Kursansvarlig", personer.get(0).verv());
        assertEquals("Fredrikstad", personer.get(0).sted());
        assertNull(personer.get(1).verv());
    }

    @Test void artiklerSkjulerKladdOgSorteresStabilt() {
        var repo = new JdbcArtikkelRepository(database);
        assertEquals(List.of(2L, 1L, 4L), repo.finnPubliserte().stream().map(a -> a.id()).toList());
        assertTrue(repo.finnPublisert(3).isEmpty());
        assertTrue(repo.finnPublisert(9999).isEmpty());
    }

    @Test void artikkelHarValgfrittLagBildeOgNorskeTegn() {
        var repo = new JdbcArtikkelRepository(database);
        var lokal = repo.finnPublisert(1).orElseThrow();
        assertEquals(10L, lokal.lokallagId());
        assertEquals("Fredrikstad", lokal.lokallagNavn());
        assertEquals("Tekst med æøå", lokal.innhold());
        assertEquals("/bilder/nyhet-1.jpg", lokal.bildesti());
        var felles = repo.finnPublisert(2).orElseThrow();
        assertNull(felles.lokallagId()); assertNull(felles.lokallagNavn()); assertNull(felles.bildesti());
    }

    @Test void adminFiltrererPaKursetsLagOgKurs() {
        assertEquals(5, admin.finnPameldinger(null, null).size());
        assertEquals(3, admin.finnPameldinger(10L, null).size());
        assertEquals(2, admin.finnPameldinger(null, 502L).size());
        assertEquals(0, admin.finnPameldinger(10L, 502L).size());
        var p = admin.finn("venter-501-a").orElseThrow();
        assertEquals(10L, p.kursLokallagId()); assertEquals("VENTELISTE", p.status());
        assertTrue(admin.finn("' OR 1=1 --").isEmpty());
    }

    @Test void adminTellerKursOgFinnerLag() {
        assertEquals(4, admin.antallKurs(null));
        assertEquals(2, admin.antallKurs(10L));
        assertEquals(1, admin.antallKurs(20L));
        assertEquals(10L, admin.finnLokallagForKurs(501).orElseThrow());
        assertTrue(admin.finnLokallagForKurs(504).isEmpty());
        assertTrue(admin.finnLokallagForKurs(9999).isEmpty());
    }

    @Test void giPlassEndrerStatusOgTrekkerBareEnPlass() throws Exception {
        assertTrue(admin.giPlass("venter-501-a"));
        assertEquals("PAMELDT", admin.finn("venter-501-a").orElseThrow().status());
        assertEquals(0, ledige(501));
        assertEquals(409, assertThrows(ApiException.class,
                () -> admin.giPlass("venter-501-a")).status());
        assertEquals(0, ledige(501));
    }

    @Test void fulltKursBeholderVentelisteOgPlasstall() throws Exception {
        assertFalse(admin.giPlass("venter-502"));
        assertEquals("VENTELISTE", admin.finn("venter-502").orElseThrow().status());
        assertEquals(0, ledige(502));
    }

    @Test void slettingAvPameldtFrigirEnPlass() throws Exception {
        admin.slett("pameldt-501");
        assertTrue(admin.finn("pameldt-501").isEmpty()); assertEquals(2, ledige(501));
        assertEquals(404, assertThrows(ApiException.class,
                () -> admin.slett("pameldt-501")).status());
        assertEquals(2, ledige(501));
    }

    @Test void slettingFraVentelisteEndrerIkkePlasstall() throws Exception {
        admin.slett("venter-501-a");
        assertTrue(admin.finn("venter-501-a").isEmpty()); assertEquals(1, ledige(501));
    }

    @Test void feilPlasstallRullerTilbakeSlettingen() throws Exception {
        sql("UPDATE kurs SET ledige_plasser = kapasitet WHERE id = 501");
        assertEquals(409, assertThrows(ApiException.class,
                () -> admin.slett("pameldt-501")).status());
        assertTrue(admin.finn("pameldt-501").isPresent()); assertEquals(3, ledige(501));
    }

    @Test void manglendeReferanseGir404() {
        assertEquals(404, assertThrows(ApiException.class, () -> admin.giPlass("ukjent")).status());
        assertEquals(404, assertThrows(ApiException.class, () -> admin.slett("ukjent")).status());
    }

    @Test void sqlFeilRullerTilbakeTrekkAvPlass() throws Exception {
        sql("CREATE TRIGGER jdbc_test_avvis_pamelding BEFORE UPDATE ON pamelding "
                + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Testfeil'");
        try {
            assertThrows(DatabaseException.class, () -> admin.giPlass("venter-501-a"));
            assertEquals(1, ledige(501));
            assertEquals("VENTELISTE", admin.finn("venter-501-a").orElseThrow().status());
        } finally { sql("DROP TRIGGER jdbc_test_avvis_pamelding"); }
    }

    @Test void sqlFeilRullerTilbakeSletting() throws Exception {
        sql("CREATE TRIGGER jdbc_test_avvis_plasstall BEFORE UPDATE ON kurs "
                + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Testfeil'");
        try {
            assertThrows(DatabaseException.class, () -> admin.slett("pameldt-501"));
            assertTrue(admin.finn("pameldt-501").isPresent()); assertEquals(1, ledige(501));
        } finally { sql("DROP TRIGGER jdbc_test_avvis_plasstall"); }
    }

    @Test void toVentendeKanIkkeFaSammeSistePlass() throws Exception {
        var svar = samtidig(() -> admin.giPlass("venter-501-a"), () -> admin.giPlass("venter-501-b"));
        assertEquals(1, svar.stream().filter(Boolean::booleanValue).count());
        assertEquals(0, ledige(501));
        assertEquals(1, List.of("venter-501-a", "venter-501-b").stream()
                .filter(ref -> "PAMELDT".equals(admin.finn(ref).orElseThrow().status())).count());
    }

    @Test void samtidigTildelingTilSammePersonTrekkerEnPlass() throws Exception {
        Callable<Boolean> forsok = () -> {
            try { return admin.giPlass("venter-501-a"); }
            catch (ApiException e) { assertEquals(409, e.status()); return false; }
        };
        assertEquals(1, samtidig(forsok, forsok).stream().filter(Boolean::booleanValue).count());
        assertEquals(0, ledige(501));
    }

    @Test void samtidigSlettingFrigirBareEnPlass() throws Exception {
        Callable<Boolean> forsok = () -> {
            try { admin.slett("pameldt-501"); return true; }
            catch (ApiException e) { assertEquals(404, e.status()); return false; }
        };
        assertEquals(1, samtidig(forsok, forsok).stream().filter(Boolean::booleanValue).count());
        assertEquals(2, ledige(501));
    }

    @Test void vanligPameldingOgAdminDelerSistePlass() throws Exception {
        var service = new PameldingService(new JdbcPameldingRepository(database),
                Clock.systemUTC(), (epost, kvittering) -> { });
        var request = new PameldingRequest(UUID.randomUUID().toString(), "Ny", "Testperson",
                "ny@example.test", "99999999", 2000, "nei", "", "", true);
        var svar = samtidig(() -> admin.giPlass("venter-501-a"),
                () -> "pameldt".equals(service.registrer(501, request).status()));
        assertEquals(1, svar.stream().filter(Boolean::booleanValue).count());
        assertEquals(0, ledige(501));
    }

    @Test void vanligPameldingKanGjentasUtenNyttPlasstrekk() throws Exception {
        var service = pameldingService();
        String nokkel = UUID.randomUUID().toString();
        var request = foresporsel(nokkel, "ny@example.test");
        var forste = service.registrer(501, request);
        var gjentatt = service.registrer(501, request);
        assertEquals(nokkel, forste.referanse());
        assertEquals(forste, gjentatt);
        assertEquals("pameldt", forste.status()); assertFalse(forste.demo());
        assertEquals(0, ledige(501));
        assertEquals(1, admin.finnPameldinger(null, 501L).stream()
                .filter(p -> nokkel.equals(p.referanse())).count());
    }

    @Test void fulltKursLagrerOgGjentarVenteliste() throws Exception {
        var service = pameldingService();
        var request = foresporsel(UUID.randomUUID().toString(), "ventende@example.test");
        var forste = service.registrer(502, request);
        assertEquals("venteliste", forste.status());
        assertEquals("VENTELISTE", admin.finn(forste.referanse()).orElseThrow().status());
        assertEquals(forste, service.registrer(502, request));
        assertEquals(0, ledige(502));
    }

    @Test void sammeEpostMedNyNokkelGir409UtenPlasstrekk() throws Exception {
        var service = pameldingService();
        service.registrer(501, foresporsel(UUID.randomUUID().toString(), "ny@example.test"));
        assertEquals(409, assertThrows(ApiException.class, () -> service.registrer(501,
                foresporsel(UUID.randomUUID().toString(), "ny@example.test"))).status());
        assertEquals(0, ledige(501));
    }

    @Test void adminKursHenterOgsaaKladderMedRiktigLag() {
        var repo = new JdbcAdminKursRepository(database);
        assertEquals(4, repo.finnAlle(null).size());
        assertEquals(2, repo.finnAlle(10L).size());
        assertEquals("KLADD", repo.finn(503).orElseThrow().status());
        assertEquals("Fredrikstad", repo.finn(501).orElseThrow().lokallagNavn());
        assertNull(repo.finn(504).orElseThrow().lokallagId());
    }

    @Test void kursOpprettesMedTellerOgRiktigLagtekst() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        long id = repo.opprett(kursData(5));
        var kurs = repo.finn(id).orElseThrow();
        assertTrue(id > 504); assertEquals(5, kurs.kapasitet()); assertEquals(5, kurs.ledigePlasser());
        assertEquals(20L, kurs.lokallagId()); assertEquals("Moss", kurs.lokallagNavn());
        assertEquals("/bilder/kurs-3.jpg", kurs.bildesti());
        assertEquals("Moss", verdi("SELECT lokallag FROM kurs WHERE id = " + id));
        assertEquals(Long.toString(id), verdi("SELECT siste_id FROM id_teller WHERE navn = 'kurs'"));
    }

    @Test void kapasitetEndresUtenAaMisteTidligereOpptattePlasser() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        assertTrue(repo.oppdater(501, kursData(6)));
        assertEquals(6, repo.finn(501).orElseThrow().kapasitet());
        assertEquals(4, ledige(501));
        assertEquals("Moss", verdi("SELECT lokallag FROM kurs WHERE id = 501"));
    }

    @Test void forLavKapasitetGirIngenDelvisKursendring() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        assertFalse(repo.oppdater(501, kursData(1)));
        assertEquals("Kurs Fredrikstad", repo.finn(501).orElseThrow().tittel());
        assertEquals(3, repo.finn(501).orElseThrow().kapasitet()); assertEquals(1, ledige(501));
    }

    @Test void kursUtenRegistreringerKanSlettes() {
        var repo = new JdbcAdminKursRepository(database);
        assertTrue(repo.slettHvisIngenPameldte(503));
        assertTrue(repo.finn(503).isEmpty());
    }

    @Test void kursMedRegistreringerIEnAvVentelisteneBeholdes() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        assertFalse(repo.slettHvisIngenPameldte(501));
        admin.slett("pameldt-502");
        assertFalse(repo.slettHvisIngenPameldte(502));
        sql("INSERT INTO venteliste (kurs_id, navn, epost) VALUES (503, 'Test', 'legacy@example.test')");
        assertFalse(repo.slettHvisIngenPameldte(503));
        assertTrue(repo.finn(503).isPresent());
    }

    @Test void samtidigOpprettelseGirUnikeKursIdEr() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        var ider = samtidig(() -> repo.opprett(kursData(5)), () -> repo.opprett(kursData(5)));
        assertNotEquals(ider.get(0), ider.get(1));
        for (long id : ider) { assertTrue(id > 504); assertTrue(repo.finn(id).isPresent()); }
    }

    @Test void feilVedKursopprettelseRullerTilbakeTelleren() throws Exception {
        sql("CREATE TRIGGER jdbc_test_avvis_plasstall BEFORE INSERT ON kurs "
                + "FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Testfeil'");
        try {
            var repo = new JdbcAdminKursRepository(database);
            assertThrows(DatabaseException.class, () -> repo.opprett(kursData(5)));
            assertEquals("0", verdi("SELECT siste_id FROM id_teller WHERE navn = 'kurs'"));
            assertEquals(4, repo.finnAlle(null).size());
        } finally { sql("DROP TRIGGER jdbc_test_avvis_plasstall"); }
    }

    @Test void samtidigKapasitetsendringOgPameldingBeholderPlasstall() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        var service = pameldingService();
        var svar = samtidig(() -> repo.oppdater(501, kursData(6)), () -> "pameldt".equals(
                service.registrer(501, foresporsel(UUID.randomUUID().toString(), "kapasitet@example.test")).status()));
        assertTrue(svar.stream().allMatch(Boolean::booleanValue));
        assertEquals(3, ledige(501)); assertEquals(6, repo.finn(501).orElseThrow().kapasitet());
    }

    @Test void adminKursKontrollererUkjenteIdEr() {
        var repo = new JdbcAdminKursRepository(database);
        assertTrue(repo.lokallagFinnes(10)); assertFalse(repo.lokallagFinnes(9999));
        assertTrue(repo.kontaktpersonFinnes(1)); assertFalse(repo.kontaktpersonFinnes(9999));
        assertTrue(repo.finn(9999).isEmpty());
        assertEquals(404, assertThrows(ApiException.class, () -> repo.oppdater(9999, kursData(5))).status());
        assertEquals(404, assertThrows(ApiException.class, () -> repo.slettHvisIngenPameldte(9999)).status());
    }

    @Test void kursvalideringBrukerDatabasensTekstgrenser() throws Exception {
        var repo = new JdbcAdminKursRepository(database);
        var service = new AdminKursService(repo);
        var bruker = new InnloggetBruker(1, "Admin", "admin@example.test", "ADMIN", null);
        var json = new ObjectMapper();
        var original = (com.fasterxml.jackson.databind.node.ObjectNode) json.valueToTree(kursSkjema(new BigDecimal("900")));
        var grenser = java.util.Map.of("tittel", 180, "tidspunkt", 60, "varighet", 80,
                "sted", 180, "tema", 60, "niva", 60, "bildesti", 255, "bildealt", 180);
        for (var grense : grenser.entrySet()) {
            var feil = original.deepCopy().put(grense.getKey(), "x".repeat(grense.getValue() + 1));
            var skjema = json.treeToValue(feil, KursSkjema.class);
            assertEquals(400, assertThrows(ApiException.class, () -> service.opprett(bruker, skjema)).status());
            original.put(grense.getKey(), "x".repeat(grense.getValue()));
        }
        long id = service.opprett(bruker, json.treeToValue(original, KursSkjema.class));
        assertTrue(repo.finn(id).isPresent());
    }

    @Test void kursprisMaaPasseDecimalTiTo() {
        var repo = new JdbcAdminKursRepository(database);
        var service = new AdminKursService(repo);
        var bruker = new InnloggetBruker(1, "Admin", "admin@example.test", "ADMIN", null);
        for (String feil : List.of("1.001", "100000000.00")) {
            assertEquals(400, assertThrows(ApiException.class,
                    () -> service.opprett(bruker, kursSkjema(new BigDecimal(feil)))).status());
        }
        long id = service.opprett(bruker, kursSkjema(new BigDecimal("99999999.99")));
        assertEquals(new BigDecimal("99999999.99"), repo.finn(id).orElseThrow().medlemspris());
    }

    @Test void apiBrukerJdbcOgBeskytterAndreLokallag() throws Exception {
        var clock = Clock.systemUTC();
        var app = Application.create(
                new KursService(new JdbcKursRepository(database), clock),
                new PameldingService(new JdbcPameldingRepository(database), clock, (e, k) -> { }),
                new NyhetService(new JdbcArtikkelRepository(database)),
                new InnloggingService(new JdbcAdministratorRepository(database)),
                new LokallagService(new JdbcLokallagRepository(database)),
                new KontaktpersonService(new JdbcKontaktpersonRepository(database)),
                new AdminPameldingService(admin),
                new AdminKursService(new JdbcAdminKursRepository(database))).start(0);
        var klient = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String adresse = "http://localhost:" + app.port();
        try {
            assertEquals(401, send(klient, adresse, "GET", "/api/admin/pameldinger", null).statusCode());
            String login = new ObjectMapper().writeValueAsString(
                    java.util.Map.of("epost", "bruker2@example.test", "passord", PASSORD));
            var innlogging = send(klient, adresse, "POST", "/api/login", login);
            assertEquals(200, innlogging.statusCode()); assertFalse(innlogging.body().contains("passordHash"));
            var rader = new ObjectMapper().readTree(
                    send(klient, adresse, "GET", "/api/admin/pameldinger", null).body());
            assertEquals(3, rader.size());
            var json = new ObjectMapper();
            assertEquals(2, json.readTree(send(klient, adresse, "GET", "/api/lokallag", null).body()).size());
            assertEquals(2, json.readTree(send(klient, adresse, "GET", "/api/kontaktpersoner", null).body()).size());
            assertEquals(3, json.readTree(send(klient, adresse, "GET", "/api/nyheter", null).body()).size());
            assertEquals(2, json.readTree(send(klient, adresse, "GET", "/api/admin/kurs", null).body()).size());
            assertEquals(404, send(klient, adresse, "GET", "/api/admin/kurs/502", null).statusCode());
            String nyttKurs = json.writeValueAsString(kursSkjema(new BigDecimal("900")));
            var opprettet = send(klient, adresse, "POST", "/api/admin/kurs", nyttKurs);
            assertEquals(201, opprettet.statusCode());
            long kursId = json.readTree(opprettet.body()).get("id").asLong();
            assertEquals(200, send(klient, adresse, "PUT", "/api/admin/kurs/" + kursId, nyttKurs).statusCode());
            assertEquals(200, send(klient, adresse, "DELETE", "/api/admin/kurs/" + kursId, null).statusCode());
            assertEquals(404, send(klient, adresse, "GET", "/api/admin/kurs/" + kursId, null).statusCode());
            assertEquals(404, send(klient, adresse, "POST",
                    "/api/admin/pameldinger/venter-502/gi-plass", null).statusCode());
            assertEquals(404, send(klient, adresse, "DELETE",
                    "/api/admin/pameldinger/pameldt-502", null).statusCode());
            assertEquals(200, send(klient, adresse, "POST",
                    "/api/admin/pameldinger/venter-501-a/gi-plass", null).statusCode());
            assertEquals(0, ledige(501));
            assertEquals(200, send(klient, adresse, "DELETE",
                    "/api/admin/pameldinger/pameldt-501", null).statusCode());
            assertEquals(1, ledige(501));
            var nyhet = send(klient, adresse, "GET", "/api/nyheter/1", null);
            assertTrue(nyhet.body().contains("Tekst med æøå"));
            assertTrue(nyhet.headers().firstValue("Content-Type").orElseThrow().contains("charset=utf-8"));
        } finally { app.stop(); }
    }

    private void pamelding(String ref, long kurs, String status, String tid) throws Exception {
        try (var c = database.connect(); var s = c.prepareStatement("""
                INSERT INTO pamelding (referanse, foresporsel_hash, kurs_id, fornavn, etternavn,
                    epost, telefon, fodselsar, medlem, medlemsnummer, kommentar, samtykke,
                    pris, kurs_tittel, kurs_dato, kurs_tidspunkt, kurs_sted, kurs_lokallag, status, opprettet)
                VALUES (?, REPEAT('a', 64), ?, 'Test', 'Person', ?, '99999999', 2000, 0, '', '',
                    1, 1200, 'Opprinnelig tittel', CURRENT_DATE + INTERVAL 30 DAY,
                    '10:00–15:00', 'Testlokale', 'Opprinnelig lag', ?, ?)
                """)) {
            s.setString(1, ref); s.setLong(2, kurs); s.setString(3, ref + "@example.test");
            s.setString(4, status); s.setString(5, tid); s.executeUpdate();
        }
    }

    private PameldingService pameldingService() {
        return new PameldingService(new JdbcPameldingRepository(database),
                Clock.systemUTC(), (epost, kvittering) -> { });
    }

    private static KursData kursData(int kapasitet) {
        return new KursData("Nytt kurs", "Testbeskrivelse", LocalDate.now().plusDays(30),
                "10:00–15:00", "1 dag", "Testlokale", 20L, "Veving", "Voksne", "Nybegynner",
                "Notatbok", new BigDecimal("900.00"), new BigDecimal("1200.00"),
                kapasitet, "PUBLISERT", 1, "/bilder/kurs-3.jpg", "Vevkurs");
    }

    private static KursSkjema kursSkjema(BigDecimal pris) {
        return new KursSkjema("Kurs fra API", "Testbeskrivelse", LocalDate.now().plusDays(30).toString(),
                "10:00–15:00", "1 dag", "Testlokale", 10L, "Veving", "Voksne", "Nybegynner",
                "Notatbok", pris, new BigDecimal("1200.00"), 5, "PUBLISERT", 1L,
                "/bilder/kurs-3.jpg", "Vevkurs");
    }

    private String verdi(String sql) throws Exception {
        try (var c = database.connect(); var s = c.createStatement(); var r = s.executeQuery(sql)) {
            assertTrue(r.next()); return r.getString(1);
        }
    }

    private static PameldingRequest foresporsel(String nokkel, String epost) {
        return new PameldingRequest(nokkel, "Ny", "Testperson", epost,
                "99999999", 2000, "nei", "", "", true);
    }

    private int ledige(long kurs) throws Exception {
        try (var c = database.connect(); var s = c.prepareStatement("SELECT ledige_plasser FROM kurs WHERE id = ?")) {
            s.setLong(1, kurs);
            try (var r = s.executeQuery()) { assertTrue(r.next()); return r.getInt(1); }
        }
    }

    private void sql(String sql) throws Exception {
        try (var c = database.connect(); var s = c.createStatement()) { s.execute(sql); }
    }

    private static <T> List<T> samtidig(Callable<T> a, Callable<T> b) throws Exception {
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var fa = pool.submit(() -> { start.await(); return a.call(); });
            var fb = pool.submit(() -> { start.await(); return b.call(); });
            start.countDown();
            return List.of(fa.get(10, TimeUnit.SECONDS), fb.get(10, TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); }
    }

    private static HttpResponse<String> send(HttpClient klient, String adresse,
            String metode, String sti, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(adresse + sti))
                .method(metode, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) request.header("Content-Type", "application/json");
        return klient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
