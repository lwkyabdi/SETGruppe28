package no.hiof.gruppe28.epost;

import no.hiof.gruppe28.dto.PameldingKvittering;

public class KonsollEpostTjeneste implements EpostTjeneste {

    @Override
    public void sendBekreftelse(String mottaker, PameldingKvittering kvittering) {
        sendMelding(mottaker, BekreftelsesTekst.emne(kvittering), BekreftelsesTekst.tekst(kvittering));
    }

    @Override
    public void sendMelding(String mottaker, String emne, String tekst) {
        System.out.println("----- E-POST (ikke sendt, bare vist) -----");
        System.out.println("Til: " + mottaker);
        System.out.println("Emne: " + emne);
        System.out.println();
        System.out.println(tekst);
        System.out.println("------------------------------------------");
    }
}
