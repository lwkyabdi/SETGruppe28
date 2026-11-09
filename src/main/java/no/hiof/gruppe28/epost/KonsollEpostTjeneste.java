package no.hiof.gruppe28.epost;

import no.hiof.gruppe28.dto.PameldingKvittering;

public class KonsollEpostTjeneste implements EpostTjeneste {

    @Override
    public void sendBekreftelse(String mottaker, PameldingKvittering kvittering) {
        System.out.println("----- E-POST (ikke sendt, bare vist) -----");
        System.out.println("Til: " + mottaker);
        System.out.println("Emne: " + lagEmne(kvittering));
        System.out.println();
        System.out.println(lagTekst(kvittering));
        System.out.println("------------------------------------------");
    }

    private String lagEmne(PameldingKvittering kvittering) {
        if ("venteliste".equals(kvittering.status())) {
            return "Du står på venteliste: " + kvittering.kursTittel();
        }
        return "Bekreftelse på påmelding: " + kvittering.kursTittel();
    }

    private String lagTekst(PameldingKvittering kvittering) {
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
