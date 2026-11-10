package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Artikkel;

import java.util.List;
import java.util.Optional;

public interface ArtikkelRepository {

    List<Artikkel> finnPubliserte();

    Optional<Artikkel> finnPublisert(long id);
}
