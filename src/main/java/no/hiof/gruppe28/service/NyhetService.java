package no.hiof.gruppe28.service;

import no.hiof.gruppe28.model.Nyhet;
import no.hiof.gruppe28.repository.NyhetRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NyhetService {

    private final NyhetRepository repository;

    public NyhetService(NyhetRepository repository) {
        this.repository = repository;
    }

    public List<Nyhet> alle() {
        return repository.findAlle().stream()
            .sorted(Comparator.comparing(Nyhet::dato).reversed()
                .thenComparing(Comparator.comparingLong(Nyhet::id).reversed()))
            .toList();
    }

    public Optional<Nyhet> finn(long id) {
        return repository.findById(id);
    }
}
