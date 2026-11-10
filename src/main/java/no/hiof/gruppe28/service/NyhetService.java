package no.hiof.gruppe28.service;

import no.hiof.gruppe28.model.Artikkel;
import no.hiof.gruppe28.repository.ArtikkelRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NyhetService {

    private final ArtikkelRepository repository;

    public NyhetService(ArtikkelRepository repository) {
        this.repository = repository;
    }

    public List<Artikkel> alle() {
        return repository.finnPubliserte().stream()
            .sorted(Comparator.comparing(Artikkel::publisertTid).reversed()
                .thenComparing(Comparator.comparingLong(Artikkel::id).reversed()))
            .toList();
    }

    public Optional<Artikkel> finn(long id) {
        return repository.finnPublisert(id);
    }
}
