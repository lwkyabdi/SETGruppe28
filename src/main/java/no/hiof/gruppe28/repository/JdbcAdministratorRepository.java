package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.Administrator;

import java.sql.SQLException;
import java.util.Optional;

public class JdbcAdministratorRepository implements AdministratorRepository {

    private final DatabaseConfig database;

    public JdbcAdministratorRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public Optional<Administrator> finnAktivMedEpost(String epost) {
        if (epost == null || epost.isBlank()) return Optional.empty();

        String sql = """
                SELECT id, navn, epost, passord_hash, rolle, lokallag_id, aktiv
                FROM administrator
                WHERE epost = ? AND aktiv = 1
                """;

        try (var c = database.connect(); var s = c.prepareStatement(sql)) {
            s.setString(1, epost.trim());
            try (var r = s.executeQuery()) {
                if (!r.next()) return Optional.empty();

                long lag = r.getLong("lokallag_id");
                Long lokallagId = r.wasNull() ? null : lag;
                return Optional.of(new Administrator(
                        r.getLong("id"), r.getString("navn"), r.getString("epost"),
                        r.getString("passord_hash"), r.getString("rolle"),
                        lokallagId, r.getBoolean("aktiv")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
