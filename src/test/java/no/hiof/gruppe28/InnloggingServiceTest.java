package no.hiof.gruppe28;

import no.hiof.gruppe28.model.Administrator;
import no.hiof.gruppe28.repository.AdministratorRepository;
import no.hiof.gruppe28.service.InnloggingService;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnloggingServiceTest {

    private final Administrator kari = new Administrator(
        1L,
        "Kari Nordmann",
        "kari@test.no",
        BCrypt.hashpw("riktig123", BCrypt.gensalt()),
        "LOKALLAG",
        2L,
        true
    );

    private final AdministratorRepository falskRepository = epost ->
        epost.equals(kari.epost()) ? Optional.of(kari) : Optional.empty();

    private final InnloggingService service = new InnloggingService(falskRepository);

    @Test
    void riktigPassordGirAdministratoren() {
        Optional<Administrator> resultat = service.loggInn("kari@test.no", "riktig123");

        assertTrue(resultat.isPresent());
        assertEquals("Kari Nordmann", resultat.get().navn());
    }

    @Test
    void feilPassordGirTomt() {
        assertTrue(service.loggInn("kari@test.no", "feil").isEmpty());
    }

    @Test
    void ukjentEpostGirTomt() {
        assertTrue(service.loggInn("ola@test.no", "riktig123").isEmpty());
    }

    @Test
    void tomtPassordGirTomt() {
        assertTrue(service.loggInn("kari@test.no", "").isEmpty());
    }
}
