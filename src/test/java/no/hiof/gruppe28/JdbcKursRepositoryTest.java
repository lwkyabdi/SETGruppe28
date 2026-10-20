package no.hiof.gruppe28;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.repository.JdbcKursRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
            s.execute("DROP TABLE IF EXISTS kurs");
            s.execute("DROP TABLE IF EXISTS kontaktperson");
            for (String file : new String[]{"01-schema.sql", "02-seed.sql"}) {
                String sql = Files.readString(Path.of("database", file), StandardCharsets.UTF_8)
                        .replaceAll("(?m)^\\s*--.*$", "");
                for (String statement : sql.split(";")) {
                    if (!statement.isBlank()) s.execute(statement);
                }
            }
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
    }
    @Test void ukjentIdReturnererTomtResultat() {
        assertTrue(repository.findPublisert(999999).isEmpty());
    }
}
