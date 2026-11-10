package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.AdminKurs;
import no.hiof.gruppe28.model.KursData;

import java.util.List;
import java.util.Optional;

public interface AdminKursRepository {

    // Alle kurs, også kladder. lokallagId null = alle lag. Sortering trengs ikke.
    List<AdminKurs> finnAlle(Long lokallagId);

    // Ett kurs, også kladd. Tom hvis id-en ikke finnes.
    Optional<AdminKurs> finn(long id);

    boolean lokallagFinnes(long lokallagId);

    boolean kontaktpersonFinnes(long kontaktId);

    // Lager kurset og gir den nye id-en.
    // Id-en hentes fra id_teller i samme transaksjon. ledige_plasser settes lik kapasitet.
    // Teksten i kurs.lokallag hentes fra lokallag.navn for lokallagId.
    long opprett(KursData data);

    // Endrer kurset i én transaksjon, med kursraden låst.
    // ledige_plasser = ledige_plasser + (ny kapasitet - gammel kapasitet).
    // Gir false og endrer ingenting hvis ledige_plasser da ville blitt under 0.
    boolean oppdater(long id, KursData data);

    // Sletter kurset bare hvis ingen rader i pamelding (eller venteliste) har dette kurs_id.
    // Gir false og sletter ingenting ellers.
    boolean slettHvisIngenPameldte(long id);
}
