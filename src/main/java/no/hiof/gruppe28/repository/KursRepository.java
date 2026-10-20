package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Kurs;
import java.util.List;
import java.util.Optional;

public interface KursRepository {
    List<Kurs> findPubliserte();
    Optional<Kurs> findPublisert(long id);
}
