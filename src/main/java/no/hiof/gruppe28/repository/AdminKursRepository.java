package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.AdminKurs;
import no.hiof.gruppe28.model.KursData;

import java.util.List;
import java.util.Optional;

public interface AdminKursRepository {


    List<AdminKurs> finnAlle(Long lokallagId);


    Optional<AdminKurs> finn(long id);

    boolean lokallagFinnes(long lokallagId);

    boolean kontaktpersonFinnes(long kontaktId);


    long opprett(KursData data);


    boolean oppdater(long id, KursData data);


    boolean slettHvisIngenPameldte(long id);
}
