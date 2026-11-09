package no.hiof.gruppe28.epost;

import no.hiof.gruppe28.dto.PameldingKvittering;

public class KonsollEpostTjeneste implements EpostTjeneste {

    @Override
    public void sendBekreftelse(String mottaker, PameldingKvittering kvittering) {
        System.out.println("----- E-POST (ikke sendt, bare vist) -----");
        System.out.println("Til: " + mottaker);
        System.out.println("Emne: " + BekreftelsesTekst.emne(kvittering));
        System.out.println();
        System.out.println(BekreftelsesTekst.tekst(kvittering));
        System.out.println("------------------------------------------");
    }
}
