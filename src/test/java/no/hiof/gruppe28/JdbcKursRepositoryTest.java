package no.hiof.gruppe28;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.repository.JdbcKursRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "MYSQL_TEST_URL", matches = ".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcKursRepositoryTest {
    private JdbcKursRepository repository;

    @BeforeAll void setup() throws Exception {
        String url = System.getenv("MYSQL_TEST_URL");
        if (!url.matches("^jdbc:mysql://[^/]+/husflid_test(\\?.*)?$")) {
            throw new IllegalArgumentException("Databasetestene krever den separate databasen husflid_test.");
        }
        var config = new DatabaseConfig(url, System.getenv("MYSQL_TEST_USER"), System.getenv("MYSQL_TEST_PASSWORD"));
        try (var c = config.connect(); var s = c.createStatement()) {
            JdbcTestskjema.gjenopprett(c);
            s.execute("""
                    UPDATE kurs
                    SET lokallag = 'Gammelt navn',
                        bildesti = '/bilder/kurs-1.jpg',
                        bildealt = 'Deltakere syr bunad'
                    WHERE id = 1
                    """);
            s.execute("UPDATE kurs SET lokallag_id = NULL WHERE id = 2");
            s.execute("""
                    UPDATE kontaktperson
                    SET verv = 'Kursansvarlig', sted = 'Fredrikstad'
                    WHERE id = 1
                    """);
        }
        repository = new JdbcKursRepository(config);
    }
    @Test void barePubliserteKursReturneres() {
        assertEquals(7, repository.findPubliserte().size());
        assertTrue(repository.findPublisert(8).isEmpty());
    }
    @Test void detaljOgKontaktHentesMedJoin() {
        var k = repository.findPublisert(1).orElseThrow();
        assertEquals("Bunadssøm for nybegynnere", k.tittel());
        assertEquals("fredrikstad@example.test", k.kontakt().epost());
        assertEquals("Kursansvarlig", k.kontakt().verv());
        assertEquals("Fredrikstad", k.kontakt().sted());
    }
    @Test void ukjentIdReturnererTomtResultat() {
        assertTrue(repository.findPublisert(999999).isEmpty());
    }
    @Test void lokallagsnavnetHentesFraKobletLokallag() {
        assertEquals("Fredrikstad", repository.findPublisert(1).orElseThrow().lokallag());
    }
    @Test void bildeOgAlternativTekstHentesFraDatabasen() {
        var kurs = repository.findPublisert(1).orElseThrow();
        assertEquals("/bilder/kurs-1.jpg", kurs.bildesti());
        assertEquals("Deltakere syr bunad", kurs.bildealt());
        assertNull(repository.findPublisert(2).orElseThrow().bildesti());
    }
    @Test void kursUtenLokallagsIdBrukerEksisterendeNavn() {
        assertEquals("Sarpsborg", repository.findPublisert(2).orElseThrow().lokallag());
    }
}
