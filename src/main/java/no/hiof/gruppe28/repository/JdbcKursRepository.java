package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.model.Kurs;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcKursRepository implements KursRepository {

    private final DatabaseConfig database;

    private static final String SELECT = """
            SELECT k.*, p.navn AS kontakt_navn, p.epost AS kontakt_epost,
                   p.telefon AS kontakt_telefon, p.bilde AS kontakt_bilde
            FROM kurs k JOIN kontaktperson p ON p.id = k.kontakt_id
            WHERE k.status = 'PUBLISERT'
            """;

    public JdbcKursRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<Kurs> findPubliserte() {
        try (Connection c = database.connect();
             PreparedStatement s = c.prepareStatement(
                     SELECT + " ORDER BY k.dato, k.id"
             );
             ResultSet rows = s.executeQuery()) {

            List<Kurs> result = new ArrayList<>();

            while (rows.next()) {
                result.add(map(rows));
            }

            return result;

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Optional<Kurs> findPublisert(long id) {
        try (Connection c = database.connect()) {
            return findPublisert(c, id, false);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    static Optional<Kurs> findPublisert(
            Connection c,
            long id,
            boolean las
    ) throws SQLException {

        String sql = SELECT
                + " AND k.id = ?"
                + (las ? " FOR UPDATE" : "");

        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, id);

            try (ResultSet rows = s.executeQuery()) {
                return rows.next()
                        ? Optional.of(map(rows))
                        : Optional.empty();
            }
        }
    }

    private static Kurs map(ResultSet r) throws SQLException {
        Date date = r.getDate("dato");

        Kontaktperson kontakt = new Kontaktperson(
                r.getLong("kontakt_id"),
                r.getString("kontakt_navn"),
                r.getString("kontakt_epost"),
                r.getString("kontakt_telefon"),
                r.getString("kontakt_bilde")
        );

        return new Kurs(
                r.getLong("id"),
                r.getString("tittel"),
                r.getString("beskrivelse"),
                date == null ? null : date.toLocalDate(),
                r.getString("tidspunkt"),
                r.getString("varighet"),
                r.getString("sted"),
                r.getString("lokallag"),
                r.getString("tema"),
                r.getString("malgruppe"),
                r.getString("niva"),
                r.getString("utstyr"),
                r.getBigDecimal("medlemspris"),
                r.getBigDecimal("ordinaerpris"),
                r.getInt("kapasitet"),
                r.getInt("ledige_plasser"),
                kontakt
        );
    }
}