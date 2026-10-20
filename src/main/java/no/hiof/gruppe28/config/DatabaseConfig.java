package no.hiof.gruppe28.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public record DatabaseConfig(String url, String username, String password) {
    public static DatabaseConfig load() throws IOException {
        Properties p = new Properties();
        Path file = Path.of("dbConfig.properties");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                p.load(reader);
            }
        }
        return new DatabaseConfig(
                value("DB_URL", "db.url", p),
                value("DB_USER", "db.username", p),
                value("DB_PASSWORD", "db.password", p));
    }

    private static String value(String environmentKey, String propertyKey, Properties p) {
        String value = System.getenv(environmentKey);
        if (value == null || value.isBlank()) value = p.getProperty(propertyKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Mangler " + propertyKey
                    + ". Kopier dbConfig.example.properties til dbConfig.properties og fyll inn lokalt.");
        }
        return value;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
