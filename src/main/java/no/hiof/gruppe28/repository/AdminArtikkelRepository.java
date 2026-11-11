package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.AdminArtikkel;
import no.hiof.gruppe28.model.ArtikkelData;

import java.util.List;
import java.util.Optional;

public interface AdminArtikkelRepository {


    List<AdminArtikkel> finnAlle(Long lokallagId);
    Optional<AdminArtikkel> finn(long id);

    boolean lokallagFinnes(long lokallagId);

    long opprett(ArtikkelData data);

    boolean oppdater(long id, ArtikkelData data);

    boolean slett(long id);
}
