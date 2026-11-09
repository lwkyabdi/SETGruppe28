package no.hiof.gruppe28.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record EpostConfig(String host, int port, String username, String password) {

    private static final Path FIL = Path.of("epostConfig.properties");

    public static boolean finnes() {
        return Files.exists(FIL);
    }

    public static EpostConfig load() throws IOException {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(FIL, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        return new EpostConfig(
            verdi("smtp.host", p),
            Integer.parseInt(verdi("smtp.port", p)),
            verdi("smtp.username", p),
            verdi("smtp.password", p));
    }

    private static String verdi(String nokkel, Properties p) {
        String verdi = p.getProperty(nokkel);
        if (verdi == null || verdi.isBlank()) {
            throw new IllegalStateException("Mangler " + nokkel + " i epostConfig.properties.");
        }
        return verdi.trim();
    }

    @Override
    public String toString() {
        return "EpostConfig[host=" + host + ", port=" + port + ", username=" + username + ", password=***]";
    }
}
