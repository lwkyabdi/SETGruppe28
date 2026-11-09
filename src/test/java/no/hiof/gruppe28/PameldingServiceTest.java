package no.hiof.gruppe28;

import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.dto.PameldingRequest;
import no.hiof.gruppe28.epost.EpostTjeneste;
import no.hiof.gruppe28.model.PameldingStatus;
import no.hiof.gruppe28.model.Registrering;
import no.hiof.gruppe28.repository.PameldingRepository;
import no.hiof.gruppe28.service.PameldingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PameldingServiceTest {

    // Fast dato, så testen gir samme resultat hver gang
    private final Clock clock = Clock.fixed(
        Instant.parse("2026-11-09T10:00:00Z"), ZoneId.of("Europe/Oslo"));

    private final PameldingKvittering kvittering = PameldingKvittering.fra(
        TestData.kurs(1, LocalDate.of(2026, 12, 1)),
        "test-referanse",
        new BigDecimal("150.00"),
        false,
        PameldingStatus.PAMELDT
    );

    // Falsk e-post: husker hvem den "sendte" til, i stedet for å sende
    private final List<String> sendtTil = new ArrayList<>();
    private final EpostTjeneste falskEpost = (mottaker, k) -> sendtTil.add(mottaker);

    private PameldingRequest gyldigPamelding() {
        return new PameldingRequest(
            UUID.randomUUID().toString(), "Ola", "Nordmann", "Ola@Example.no",
            "12345678", 1990, "nei", "", "", true);
    }

    @Test
    void sender_epost_naar_pameldingen_er_ny() {
        PameldingRepository repository = (kursId, data, idag) -> new Registrering(kvittering, true);
        var service = new PameldingService(repository, clock, falskEpost);

        service.registrer(1, gyldigPamelding());

        assertEquals(List.of("ola@example.no"), sendtTil);
    }

    @Test
    void sender_ikke_epost_naar_pameldingen_fantes_fra_for() {
        PameldingRepository repository = (kursId, data, idag) -> new Registrering(kvittering, false);
        var service = new PameldingService(repository, clock, falskEpost);

        service.registrer(1, gyldigPamelding());

        assertTrue(sendtTil.isEmpty());
    }

    @Test
    void pameldingen_gaar_gjennom_selv_om_eposten_feiler() {
        PameldingRepository repository = (kursId, data, idag) -> new Registrering(kvittering, true);
        EpostTjeneste odelagtEpost = (mottaker, k) -> {
            throw new RuntimeException("E-postserveren er nede");
        };
        var service = new PameldingService(repository, clock, odelagtEpost);

        PameldingKvittering svar = service.registrer(1, gyldigPamelding());

        assertSame(kvittering, svar);
    }
}
