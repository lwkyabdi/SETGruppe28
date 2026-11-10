package no.hiof.gruppe28.service;

import no.hiof.gruppe28.model.Lokallag;
import no.hiof.gruppe28.repository.LokallagRepository;

import java.util.List;

public class LokallagService {

    private final LokallagRepository repository;

    public LokallagService(LokallagRepository repository) {
        this.repository = repository;
    }

    public List<Lokallag> alle() {
        return repository.finnAktive();
    }
}
