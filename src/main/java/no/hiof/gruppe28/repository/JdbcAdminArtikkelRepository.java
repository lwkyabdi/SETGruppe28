package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.AdminArtikkel;
import no.hiof.gruppe28.model.ArtikkelData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAdminArtikkelRepository implements AdminArtikkelRepository {

    private final DatabaseConfig database;

    private static final String SELECT = """
            SELECT a.id, a.tittel, a.ingress, a.innhold, a.bildesti, a.bildealt,
                   a.lokallag_id, l.navn AS lokallag_navn, a.status, a.publisert_tid
            FROM artikkel a
            LEFT JOIN lokallag l ON l.id = a.lokallag_id
            """;

    public JdbcAdminArtikkelRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<AdminArtikkel> finnAlle(Long lokallagId) {
        String sql = SELECT + (lokallagId == null ? "" : " WHERE a.lokallag_id = ?")
                + " ORDER BY a.publisert_tid DESC, a.id DESC";
        try (var c = database.connect(); var s = c.prepareStatement(sql)) {
            if (lokallagId != null) s.setLong(1, lokallagId);
            try (var r = s.executeQuery()) {
                List<AdminArtikkel> resultat = new ArrayList<>();
                while (r.next()) resultat.add(map(r));
                return resultat;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Optional<AdminArtikkel> finn(long id) {
        try (var c = database.connect();
             var s = c.prepareStatement(SELECT + " WHERE a.id = ?")) {
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
        try (var c = database.connect();
             var s = c.prepareStatement("SELECT 1 FROM lokallag WHERE id = ?")) {
            s.setLong(1, lokallagId);
            try (var r = s.executeQuery()) { return r.next(); }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long opprett(ArtikkelData data) {
        return transaksjon(c -> {
            try (var s = c.prepareStatement("""
                    INSERT INTO artikkel (tittel, ingress, innhold, bildesti, bildealt,
                                          lokallag_id, status, publisert_tid)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS)) {
                fyll(s, data);
                s.executeUpdate();
                try (var n = s.getGeneratedKeys()) {
                    if (!n.next()) throw new SQLException("Databasen returnerte ingen artikkel-ID.");
                    return n.getLong(1);
                }
            }
        });
    }

    @Override
    public boolean oppdater(long id, ArtikkelData data) {
        return transaksjon(c -> {
            try (var s = c.prepareStatement("SELECT id FROM artikkel WHERE id = ? FOR UPDATE")) {
                s.setLong(1, id);
                try (var r = s.executeQuery()) { if (!r.next()) return false; }
            }
            try (var s = c.prepareStatement("""
                    UPDATE artikkel SET tittel = ?, ingress = ?, innhold = ?, bildesti = ?,
                        bildealt = ?, lokallag_id = ?, status = ?, publisert_tid = ?
                    WHERE id = ?
                    """)) {
                fyll(s, data);
                s.setLong(9, id);
                s.executeUpdate();
            }
            return true;
        });
    }

    @Override
    public boolean slett(long id) {
        try (var c = database.connect();
             var s = c.prepareStatement("DELETE FROM artikkel WHERE id = ?")) {
            s.setLong(1, id);
            return s.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private static void fyll(PreparedStatement s, ArtikkelData d) throws SQLException {
        s.setString(1, d.tittel());
        s.setString(2, d.ingress());
        s.setString(3, d.innhold());
        s.setString(4, d.bildesti());
        s.setString(5, d.bildealt());
        if (d.lokallagId() == null) s.setNull(6, Types.BIGINT);
        else s.setLong(6, d.lokallagId());
        s.setString(7, d.status());
        s.setTimestamp(8, d.publisertTid() == null ? null : Timestamp.valueOf(d.publisertTid()));
    }

    private static AdminArtikkel map(ResultSet r) throws SQLException {
        long lag = r.getLong("lokallag_id");
        Long lokallagId = r.wasNull() ? null : lag;
        Timestamp tid = r.getTimestamp("publisert_tid");
        return new AdminArtikkel(
                r.getLong("id"), r.getString("tittel"), r.getString("ingress"),
                r.getString("innhold"), r.getString("bildesti"), r.getString("bildealt"),
                lokallagId, r.getString("lokallag_navn"), r.getString("status"),
                tid == null ? null : tid.toLocalDateTime()
        );
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

    @FunctionalInterface
    private interface Arbeid<T> {
        T utfor(Connection c) throws SQLException;
    }
}
