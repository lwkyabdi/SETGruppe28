package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Administrator;

import java.util.Optional;

public interface AdministratorRepository {

    Optional<Administrator> finnAktivMedEpost(String epost);
}

