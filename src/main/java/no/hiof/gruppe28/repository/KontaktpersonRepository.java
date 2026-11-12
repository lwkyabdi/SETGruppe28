package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Kontaktperson;

import java.util.List;

public interface KontaktpersonRepository {

    List<Kontaktperson> finnForKontaktside();

    // Alle kontaktpersoner, også de som ikke vises på kontaktsiden.
    // Brukes av admin når et kurs skal få en kontaktperson.
    default List<Kontaktperson> finnAlle() {
        return finnForKontaktside();
    }
}
