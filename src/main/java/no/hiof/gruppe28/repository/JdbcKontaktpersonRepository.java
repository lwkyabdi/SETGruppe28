package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.model.Kontaktperson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcKontaktpersonRepository implements KontaktpersonRepository {

    private final DatabaseConfig database;

    public JdbcKontaktpersonRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public List<Kontaktperson> finnForKontaktside() {
        return hent("""
                SELECT id, navn, epost, telefon, bilde, verv, sted
                FROM kontaktperson
                WHERE vis_pa_kontaktside = 1
                ORDER BY navn, id
                """);
    }

    @Override
    public List<Kontaktperson> finnAlle() {
        return hent("""
                SELECT id, navn, epost, telefon, bilde, verv, sted
                FROM kontaktperson
                ORDER BY navn, id
                """);
    }

    private List<Kontaktperson> hent(String sql) {
        try (var c = database.connect(); var s = c.prepareStatement(sql);
             var r = s.executeQuery()) {
            List<Kontaktperson> resultat = new ArrayList<>();
            while (r.next()) {
                resultat.add(new Kontaktperson(
                    r.getLong("id"), r.getString("navn"), r.getString("epost"),
                    r.getString("telefon"), r.getString("bilde"),
                    r.getString("verv"), r.getString("sted")
                ));
            }
            return resultat;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
