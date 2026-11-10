package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.Artikkel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcArtikkelRepository implements ArtikkelRepository {

    private final DatabaseConfig database;

    private static final String SELECT = """
            SELECT a.id, a.tittel, a.ingress, a.innhold, a.bildesti, a.bildealt,
                   a.lokallag_id, l.navn AS lokallag_navn, a.publisert_tid
            FROM artikkel a
            LEFT JOIN lokallag l ON l.id = a.lokallag_id
            WHERE a.status = 'PUBLISERT' AND a.publisert_tid IS NOT NULL
            """;

    public JdbcArtikkelRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<Artikkel> finnPubliserte() {
        try (var c = database.connect();
             var s = c.prepareStatement(SELECT + " ORDER BY a.publisert_tid DESC, a.id DESC");
             var r = s.executeQuery()) {
            List<Artikkel> resultat = new ArrayList<>();
            while (r.next()) resultat.add(map(r));
            return resultat;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Optional<Artikkel> finnPublisert(long id) {
        try (var c = database.connect();
             var s = c.prepareStatement(SELECT + " AND a.id = ?")) {
            s.setLong(1, id);
            try (var r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private static Artikkel map(ResultSet r) throws SQLException {
        long lag = r.getLong("lokallag_id");
        Long lokallagId = r.wasNull() ? null : lag;
        return new Artikkel(
                r.getLong("id"), r.getString("tittel"), r.getString("ingress"),
                r.getString("innhold"), r.getString("bildesti"), r.getString("bildealt"),
                lokallagId, r.getString("lokallag_navn"),
                r.getTimestamp("publisert_tid").toLocalDateTime()
        );
    }
}
