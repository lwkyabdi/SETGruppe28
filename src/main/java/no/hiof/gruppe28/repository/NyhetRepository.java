package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Nyhet;

import java.util.List;
import java.util.Optional;

public interface NyhetRepository {
    List<Nyhet> findAlle();

    Optional<Nyhet> findById(long id);
}

