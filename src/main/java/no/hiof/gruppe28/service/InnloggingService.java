package no.hiof.gruppe28.service;

import no.hiof.gruppe28.model.Administrator;
import no.hiof.gruppe28.repository.AdministratorRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class InnloggingService {

    private final AdministratorRepository repository;

    public InnloggingService(AdministratorRepository repository) {
        this.repository = repository;
    }

    public Optional<Administrator> loggInn(String epost, String passord) {
        if (epost == null || epost.isBlank() || passord == null || passord.isBlank()) {
            return Optional.empty();
        }

        Optional<Administrator> funnet = repository.finnAktivMedEpost(epost.trim());
        if (funnet.isEmpty()) {
            return Optional.empty();
        }

        Administrator admin = funnet.get();
        if (passordStemmer(passord, admin.passordHash())) {
            return Optional.of(admin);
        }
        return Optional.empty();
    }

    private boolean passordStemmer(String passord, String hash) {
        try {
            return BCrypt.checkpw(passord, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}



