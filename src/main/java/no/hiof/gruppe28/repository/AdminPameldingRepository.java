package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.AdminPamelding;

import java.util.List;
import java.util.Optional;

public interface AdminPameldingRepository {

    List<AdminPamelding> finnPameldinger(Long lokallagId, Long kursId);

    Optional<AdminPamelding> finn(String referanse);

    int antallKurs(Long lokallagId);

    Optional<Long> finnLokallagForKurs(long kursId);

    boolean giPlass(String referanse);

    void slett(String referanse);
}
