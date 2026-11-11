package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.AdminPamelding;
import no.hiof.gruppe28.service.ApiException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAdminPameldingRepository implements AdminPameldingRepository {

    private final DatabaseConfig database;

    private static final String SELECT = """
            SELECT p.referanse, p.kurs_id, k.tittel AS kurs_tittel, k.lokallag_id,
                   p.fornavn, p.etternavn, p.epost, p.telefon,
                   p.medlem, p.status, p.opprettet
            FROM pamelding p
            JOIN kurs k ON k.id = p.kurs_id
            """;

    public JdbcAdminPameldingRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<AdminPamelding> finnPameldinger(Long lokallagId, Long kursId) {
        String sql = SELECT + " WHERE 1 = 1"
                + (lokallagId == null ? "" : " AND k.lokallag_id = ?")
                + (kursId == null ? "" : " AND k.id = ?")
                + " ORDER BY CASE WHEN p.status = 'PAMELDT' THEN 0 ELSE 1 END,"
                + " p.opprettet, p.referanse";

        try (var c = database.connect(); var s = c.prepareStatement(sql)) {
            int indeks = 1;
            if (lokallagId != null) s.setLong(indeks++, lokallagId);
            if (kursId != null) s.setLong(indeks, kursId);
            try (var r = s.executeQuery()) {
                List<AdminPamelding> resultat = new ArrayList<>();
                while (r.next()) resultat.add(map(r));
                return resultat;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Optional<AdminPamelding> finn(String referanse) {
        try (var c = database.connect();
             var s = c.prepareStatement(SELECT + " WHERE p.referanse = ?")) {
            s.setString(1, referanse);
            try (var r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int antallKurs(Long lokallagId) {
        String sql = "SELECT COUNT(*) FROM kurs"
                + (lokallagId == null ? "" : " WHERE lokallag_id = ?");
        try (var c = database.connect(); var s = c.prepareStatement(sql)) {
            if (lokallagId != null) s.setLong(1, lokallagId);
            try (var r = s.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Optional<Long> finnLokallagForKurs(long kursId) {
        try (var c = database.connect();
             var s = c.prepareStatement("SELECT lokallag_id FROM kurs WHERE id = ?")) {
            s.setLong(1, kursId);
            try (var r = s.executeQuery()) {
                if (!r.next()) return Optional.empty();
                long lag = r.getLong(1);
                return r.wasNull() ? Optional.empty() : Optional.of(lag);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean giPlass(String referanse) {
        return endre(referanse, (c, rad) -> {
            if (!"VENTELISTE".equals(rad.status())) {
                throw new ApiException(409, "Personen er allerede påmeldt.");
            }
            if (rad.ledigePlasser() == 0) return false;

            try (var s = c.prepareStatement("""
                    UPDATE kurs SET ledige_plasser = ledige_plasser - 1
                    WHERE id = ? AND ledige_plasser > 0
                    """)) {
                s.setLong(1, rad.kursId());
                if (s.executeUpdate() != 1) {
                    throw new ApiException(409, "Kurset er fullt.");
                }
            }
            try (var s = c.prepareStatement("""
                    UPDATE pamelding SET status = 'PAMELDT'
                    WHERE referanse = ? AND kurs_id = ? AND status = 'VENTELISTE'
                    """)) {
                s.setString(1, referanse);
                s.setLong(2, rad.kursId());
                if (s.executeUpdate() != 1) throw endret();
            }
            return true;
        });
    }

    @Override
    public void slett(String referanse) {
        endre(referanse, (c, rad) -> {
            try (var s = c.prepareStatement(
                    "DELETE FROM pamelding WHERE referanse = ? AND kurs_id = ?")) {
                s.setString(1, referanse);
                s.setLong(2, rad.kursId());
                if (s.executeUpdate() != 1) throw endret();
            }
            if ("PAMELDT".equals(rad.status())) {
                try (var s = c.prepareStatement("""
                        UPDATE kurs SET ledige_plasser = ledige_plasser + 1
                        WHERE id = ? AND ledige_plasser < kapasitet
                        """)) {
                    s.setLong(1, rad.kursId());
                    if (s.executeUpdate() != 1) {
                        throw new ApiException(409,
                                "Plasstallene stemmer ikke. Påmeldingen er ikke slettet.");
                    }
                }
            }
            return null;
        });
    }

    private <T> T endre(String referanse, Endring<T> endring) {
        try (var c = database.connect()) {
            // Første lesing i selve transaksjonen skal ta lås, uten et eldre øyeblikksbilde.
            long kursId = finnKursId(c, referanse);
            c.setAutoCommit(false);
            try {
                LasteRader rad = las(c, referanse, kursId);
                T resultat = endring.utfor(c, rad);
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

    private static long finnKursId(Connection c, String referanse) throws SQLException {
        try (var s = c.prepareStatement("SELECT kurs_id FROM pamelding WHERE referanse = ?")) {
            s.setString(1, referanse);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw mangler();
                return r.getLong(1);
            }
        }
    }

    private static LasteRader las(Connection c, String referanse, long kursId) throws SQLException {
        // Samme låserekkefølge som vanlig påmelding: kurset før påmeldingen.
        int ledige;
        try (var s = c.prepareStatement(
                "SELECT ledige_plasser FROM kurs WHERE id = ? FOR UPDATE")) {
            s.setLong(1, kursId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw mangler();
                ledige = r.getInt(1);
            }
        }
        try (var s = c.prepareStatement(
                "SELECT kurs_id, status FROM pamelding WHERE referanse = ? FOR UPDATE")) {
            s.setString(1, referanse);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw mangler();
                if (r.getLong("kurs_id") != kursId) throw endret();
                return new LasteRader(kursId, r.getString("status"), ledige);
            }
        }
    }

    private static AdminPamelding map(ResultSet r) throws SQLException {
        long lag = r.getLong("lokallag_id");
        Long lokallagId = r.wasNull() ? null : lag;
        var opprettet = r.getTimestamp("opprettet");
        return new AdminPamelding(
                r.getString("referanse"), r.getLong("kurs_id"), r.getString("kurs_tittel"),
                lokallagId, r.getString("fornavn"), r.getString("etternavn"),
                r.getString("epost"), r.getString("telefon"), r.getBoolean("medlem"),
                r.getString("status"), opprettet == null ? null : opprettet.toLocalDateTime()
        );
    }

    private static ApiException mangler() {
        return new ApiException(404, "Påmeldingen finnes ikke.");
    }

    private static ApiException endret() {
        return new ApiException(409, "Påmeldingen er endret. Last siden på nytt.");
    }

    private record LasteRader(long kursId, String status, int ledigePlasser) { }

    @FunctionalInterface
    private interface Endring<T> {
        T utfor(Connection c, LasteRader rad) throws SQLException;
    }
}
