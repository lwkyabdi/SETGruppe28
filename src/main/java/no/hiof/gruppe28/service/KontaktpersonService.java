package no.hiof.gruppe28.service;

import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.repository.KontaktpersonRepository;

import java.util.List;

public class KontaktpersonService {

    private final KontaktpersonRepository repository;

    public KontaktpersonService(KontaktpersonRepository repository) {
        this.repository = repository;
    }

    public List<Kontaktperson> forKontaktside() {
        return repository.finnForKontaktside();
    }

    public List<Kontaktperson> alle() {
        return repository.finnAlle();
    }
}
