package no.hiof.gruppe28;

import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.epost.BekreftelsesTekst;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BekreftelsesTekstTest {

    private PameldingKvittering kvittering(String lokallag, String status) {
        return new PameldingKvittering("abc-123", 3, "Vevkurs", "2026-12-01", "18:00",
            "Moss", lokallag, new BigDecimal("900"), 5, true, status);
    }

    @Test
    void hilsenenBrukerKursetsLag() {
        String tekst = BekreftelsesTekst.tekst(kvittering("Moss", "pameldt"));

        assertTrue(tekst.endsWith("Hilsen Moss Husflidslag"));
    }

    @Test
    void fulltNavnFaarIkkeHusflidslagToGanger() {
        String tekst = BekreftelsesTekst.tekst(kvittering("Moss Husflidslag", "pameldt"));

        assertTrue(tekst.endsWith("Hilsen Moss Husflidslag"));
    }

    @Test
    void ventelisteSierAtKursetErFullt() {
        var venteliste = kvittering("Moss", "venteliste");

        assertTrue(BekreftelsesTekst.emne(venteliste).startsWith("Du står på venteliste"));
        assertTrue(BekreftelsesTekst.tekst(venteliste).contains("Kurset er fullt"));
    }

    @Test
    void pameldtFaarBekreftelseMedReferanse() {
        var pameldt = kvittering("Moss", "pameldt");

        assertTrue(BekreftelsesTekst.emne(pameldt).startsWith("Bekreftelse på påmelding"));
        assertTrue(BekreftelsesTekst.tekst(pameldt).contains("Referanse: abc-123"));
    }
}
