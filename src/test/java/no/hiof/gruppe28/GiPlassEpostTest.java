package no.hiof.gruppe28;

import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.epost.EpostTjeneste;
import no.hiof.gruppe28.service.AdminPameldingService;
import no.hiof.gruppe28.service.ApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GiPlassEpostTest {

    private static final InnloggetBruker ADMIN =
        new InnloggetBruker(1, "Admin", "admin@test.no", "ADMIN", null);

    private final List<String> sendtTil = new ArrayList<>();
    private final List<String> emner = new ArrayList<>();

    private final EpostTjeneste falskEpost = new EpostTjeneste() {
        public void sendBekreftelse(String mottaker, PameldingKvittering kvittering) { }

        public void sendMelding(String mottaker, String emne, String tekst) {
            sendtTil.add(mottaker);
            emner.add(emne);
        }
    };

    @Test
    void giPlassSenderEpostTilPersonen() {
        var repo = new AdminPameldingApiTest.FalskAdminPameldingRepository();
        var service = new AdminPameldingService(repo, falskEpost);

        service.giPlass(ADMIN, "lag1-venter");

        assertEquals(List.of("lag1-venter@test.no"), sendtTil);
        assertTrue(emner.get(0).contains("Kurs 10"));
    }

    @Test
    void fulltKursSenderIngenEpost() {
        var repo = new AdminPameldingApiTest.FalskAdminPameldingRepository();
        repo.ledigePlasser = 0;
        var service = new AdminPameldingService(repo, falskEpost);

        ApiException feil = assertThrows(ApiException.class, () -> service.giPlass(ADMIN, "lag1-venter"));

        assertEquals(409, feil.status());
        assertTrue(sendtTil.isEmpty());
    }

    @Test
    void personenFarPlassenSelvOmEpostenFeiler() {
        var repo = new AdminPameldingApiTest.FalskAdminPameldingRepository();
        EpostTjeneste odelagt = new EpostTjeneste() {
            public void sendBekreftelse(String mottaker, PameldingKvittering kvittering) { }

            public void sendMelding(String mottaker, String emne, String tekst) {
                throw new IllegalStateException("E-postserveren svarer ikke");
            }
        };
        var service = new AdminPameldingService(repo, odelagt);

        service.giPlass(ADMIN, "lag1-venter");

        assertEquals("PAMELDT", repo.finn("lag1-venter").orElseThrow().status());
    }
}
