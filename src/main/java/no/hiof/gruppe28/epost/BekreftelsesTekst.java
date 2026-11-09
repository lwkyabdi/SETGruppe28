package no.hiof.gruppe28.epost;

import no.hiof.gruppe28.dto.PameldingKvittering;

public final class BekreftelsesTekst {

    private BekreftelsesTekst() {
    }

    public static String emne(PameldingKvittering kvittering) {
        if ("venteliste".equals(kvittering.status())) {
            return "Du står på venteliste: " + kvittering.kursTittel();
        }
        return "Bekreftelse på påmelding: " + kvittering.kursTittel();
    }

    public static String tekst(PameldingKvittering kvittering) {
        String innledning;
        if ("venteliste".equals(kvittering.status())) {
            innledning = "Kurset er fullt, så du står på venteliste. Vi gir beskjed hvis du får plass.";
        } else {
            innledning = "Takk for påmeldingen! Du har fått plass på kurset.";
        }

        return innledning + "\n\n"
            + "Kurs: " + kvittering.kursTittel() + "\n"
            + "Dato: " + kvittering.dato() + "\n"
            + "Tid: " + kvittering.tidspunkt() + "\n"
            + "Sted: " + kvittering.sted() + "\n"
            + "Pris: " + kvittering.pris() + " kr\n"
            + "Referanse: " + kvittering.referanse() + "\n\n"
            + "Hilsen Fredrikstad Husflidslag";
    }
}

