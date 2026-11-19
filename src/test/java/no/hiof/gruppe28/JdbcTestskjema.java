package no.hiof.gruppe28;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

final class JdbcTestskjema {
    private JdbcTestskjema() { }

    static void gjenopprett(Connection c) throws SQLException, IOException {
        if (!"husflid_test".equals(c.getCatalog())) {
            throw new IllegalStateException("Skjemaet kan bare tilbakestilles i husflid_test.");
        }
        try (var s = c.createStatement()) {
            // En eldre testdatabase kan fortsatt ha de tre tabellene fra migrering 04.
            for (String tabell : List.of("venteliste", "pamelding", "aktivitet", "artikkel",
                    "medlemsinteresse", "administrator", "kurs", "kontaktperson", "lokallag",
                    "id_teller", "kontaktmelding")) {
                s.execute("DROP TABLE IF EXISTS " + tabell);
            }
            for (String fil : List.of("01-schema.sql", "02-seed.sql", "03-pamelding.sql",
                    "04-frontend.sql", "05-kurs-lokallag.sql", "06-pamelding-status.sql",
                    "07-kurs-avlyst.sql", "08-pamelding-ko-historikk.sql", "09-fjern-ubrukte-tabeller.sql")) {
                kjorFil(s, fil);
            }
        }
    }

    static void kjorFil(Statement s, String fil) throws SQLException, IOException {
        String skilletegn = ";";
        var sql = new StringBuilder();
        for (String linje : Files.readAllLines(Path.of("database", fil))) {
            String tekst = linje.strip();
            if (tekst.isEmpty() || tekst.startsWith("--")) continue;
            if (tekst.startsWith("DELIMITER ")) {
                skilletegn = tekst.substring("DELIMITER ".length()).strip();
            } else if (tekst.endsWith(skilletegn)) {
                sql.append(tekst, 0, tekst.length() - skilletegn.length());
                s.execute(sql.toString());
                sql.setLength(0);
            } else {
                sql.append(linje).append('\n');
            }
        }
        if (!sql.isEmpty()) throw new IOException("Uavsluttet SQL-setning i " + fil);
    }
}
