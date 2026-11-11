package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.AdminKurs;
import no.hiof.gruppe28.model.KursData;
import no.hiof.gruppe28.service.ApiException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAdminKursRepository implements AdminKursRepository {

    private final DatabaseConfig database;
    private static final String SELECT = """
            SELECT k.*, COALESCE(l.navn, k.lokallag) AS lokallag_navn
            FROM kurs k LEFT JOIN lokallag l ON l.id = k.lokallag_id
            """;

    public JdbcAdminKursRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<AdminKurs> finnAlle(Long lokallagId) {
        String sql = SELECT + (lokallagId == null ? "" : " WHERE k.lokallag_id = ?")
                + " ORDER BY k.dato IS NULL, k.dato, k.id";
        try (var c = database.connect(); var s = c.prepareStatement(sql)) {
            if (lokallagId != null) s.setLong(1, lokallagId);
            try (var r = s.executeQuery()) {
                List<AdminKurs> resultat = new ArrayList<>();
                while (r.next()) resultat.add(map(r));
                return resultat;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Optional<AdminKurs> finn(long id) {
        try (var c = database.connect(); var s = c.prepareStatement(SELECT + " WHERE k.id = ?")) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean lokallagFinnes(long lokallagId) {
        return finnes("SELECT 1 FROM lokallag WHERE id = ?", lokallagId);
    }

    @Override
    public boolean kontaktpersonFinnes(long kontaktId) {
        return finnes("SELECT 1 FROM kontaktperson WHERE id = ?", kontaktId);
    }

    @Override
    public long opprett(KursData data) {
        return transaksjon(c -> {
            long id = nesteId(c);
            String navn = lagNavn(c, data.lokallagId());
            try (var s = c.prepareStatement("""
                    INSERT INTO kurs (tittel, beskrivelse, dato, tidspunkt, varighet, sted,
                        lokallag, lokallag_id, tema, malgruppe, niva, utstyr, medlemspris,
                        ordinaerpris, kapasitet, status, kontakt_id, bildesti, bildealt,
                        ledige_plasser, id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """)) {
                fyll(s, data, navn, data.kapasitet(), id);
                s.executeUpdate();
            }
            return id;
        });
    }

    @Override
    public boolean oppdater(long id, KursData data) {
        return transaksjon(c -> {
            var gammel = lasKurs(c, id);
            long ledige = (long) gammel.ledige() + data.kapasitet() - gammel.kapasitet();
            if (ledige < 0) return false;

            String navn = lagNavn(c, data.lokallagId());
            try (var s = c.prepareStatement("""
                    UPDATE kurs SET tittel = ?, beskrivelse = ?, dato = ?, tidspunkt = ?,
                        varighet = ?, sted = ?, lokallag = ?, lokallag_id = ?, tema = ?,
                        malgruppe = ?, niva = ?, utstyr = ?, medlemspris = ?, ordinaerpris = ?,
                        kapasitet = ?, status = ?, kontakt_id = ?, bildesti = ?, bildealt = ?,
                        ledige_plasser = ? WHERE id = ?
                    """)) {
                fyll(s, data, navn, Math.toIntExact(ledige), id);
                s.executeUpdate();
            }
            return true;
        });
    }

    @Override
    public boolean slettHvisIngenPameldte(long id) {
        return transaksjon(c -> {
            lasKurs(c, id);
            for (String sql : List.of("SELECT 1 FROM pamelding WHERE kurs_id = ? LIMIT 1",
                    "SELECT 1 FROM venteliste WHERE kurs_id = ? LIMIT 1")) {
                try (var s = c.prepareStatement(sql)) {
                    s.setLong(1, id);
                    try (var r = s.executeQuery()) { if (r.next()) return false; }
                }
            }
            try (var s = c.prepareStatement("DELETE FROM kurs WHERE id = ?")) {
                s.setLong(1, id);
                s.executeUpdate();
            }
            return true;
        });
    }

    private boolean finnes(String sql, long id) {
        try (var c = database.connect(); var s = c.prepareStatement(sql)) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) { return r.next(); }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private static String lagNavn(Connection c, Long id) throws SQLException {
        if (id == null) throw new ApiException(400, "Velg et lokallag som finnes.");
        try (var s = c.prepareStatement("SELECT navn FROM lokallag WHERE id = ?")) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new ApiException(400, "Velg et lokallag som finnes.");
                return r.getString(1);
            }
        }
    }

    private static long nesteId(Connection c) throws SQLException {
        long sist;
        try (var s = c.prepareStatement("SELECT siste_id FROM id_teller WHERE navn = 'kurs' FOR UPDATE");
             var r = s.executeQuery()) {
            if (!r.next()) throw new SQLException("Mangler id_teller for kurs. Kjør database/04-frontend.sql.");
            sist = r.getLong(1);
        }
        try (var s = c.prepareStatement("SELECT COALESCE(MAX(id), 0) FROM kurs");
             var r = s.executeQuery()) {
            r.next();
            sist = Math.max(sist, r.getLong(1));
        }
        long nyId = Math.addExact(sist, 1);
        try (var s = c.prepareStatement("UPDATE id_teller SET siste_id = ? WHERE navn = 'kurs'")) {
            s.setLong(1, nyId);
            s.executeUpdate();
        }
        return nyId;
    }

    private static LastePlasser lasKurs(Connection c, long id) throws SQLException {
        try (var s = c.prepareStatement("SELECT kapasitet, ledige_plasser FROM kurs WHERE id = ? FOR UPDATE")) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new ApiException(404, "Kurset finnes ikke.");
                return new LastePlasser(r.getInt("kapasitet"), r.getInt("ledige_plasser"));
            }
        }
    }

    private static void fyll(PreparedStatement s, KursData d, String lag, int ledige, long id)
            throws SQLException {
        s.setString(1, d.tittel()); s.setString(2, d.beskrivelse());
        s.setDate(3, d.dato() == null ? null : Date.valueOf(d.dato()));
        s.setString(4, d.tidspunkt()); s.setString(5, d.varighet()); s.setString(6, d.sted());
        s.setString(7, lag); s.setLong(8, d.lokallagId()); s.setString(9, d.tema());
        s.setString(10, d.malgruppe()); s.setString(11, d.niva()); s.setString(12, d.utstyr());
        s.setBigDecimal(13, d.medlemspris()); s.setBigDecimal(14, d.ordinaerpris());
        s.setInt(15, d.kapasitet()); s.setString(16, d.status()); s.setLong(17, d.kontaktId());
        s.setString(18, d.bildesti()); s.setString(19, d.bildealt());
        s.setInt(20, ledige); s.setLong(21, id);
    }

    private <T> T transaksjon(Arbeid<T> arbeid) {
        try (var c = database.connect()) {
            c.setAutoCommit(false);
            try {
                T resultat = arbeid.utfor(c);
                c.commit();
                return resultat;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); }
                catch (SQLException rollbackFeil) { e.addSuppressed(rollbackFeil); }
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private static AdminKurs map(ResultSet r) throws SQLException {
        long lag = r.getLong("lokallag_id");
        Long lokallagId = r.wasNull() ? null : lag;
        var dato = r.getDate("dato");
        return new AdminKurs(r.getLong("id"), r.getString("tittel"), r.getString("beskrivelse"),
                dato == null ? null : dato.toLocalDate(), r.getString("tidspunkt"),
                r.getString("varighet"), r.getString("sted"), lokallagId,
                r.getString("lokallag_navn"), r.getString("tema"), r.getString("malgruppe"),
                r.getString("niva"), r.getString("utstyr"), r.getBigDecimal("medlemspris"),
                r.getBigDecimal("ordinaerpris"), r.getInt("kapasitet"), r.getInt("ledige_plasser"),
                r.getString("status"), r.getLong("kontakt_id"), r.getString("bildesti"), r.getString("bildealt"));
    }

    private record LastePlasser(int kapasitet, int ledige) { }

    @FunctionalInterface
    private interface Arbeid<T> {
        T utfor(Connection c) throws SQLException;
    }
}
