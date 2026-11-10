package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.Lokallag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcLokallagRepository implements LokallagRepository {

    private final DatabaseConfig database;

    public JdbcLokallagRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<Lokallag> finnAktive() {
        String sql = """
                SELECT id, slug, navn, beskrivelse, adresse, epost, telefon, bildesti
                FROM lokallag
                WHERE aktiv = 1
                ORDER BY navn, id
                """;

        try (var c = database.connect(); var s = c.prepareStatement(sql);
             var r = s.executeQuery()) {
            List<Lokallag> resultat = new ArrayList<>();
            while (r.next()) {
                resultat.add(new Lokallag(
                        r.getLong("id"), r.getString("slug"), r.getString("navn"),
                        r.getString("beskrivelse"), r.getString("adresse"),
                        r.getString("epost"), r.getString("telefon"), r.getString("bildesti")
                ));
            }
            return resultat;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
