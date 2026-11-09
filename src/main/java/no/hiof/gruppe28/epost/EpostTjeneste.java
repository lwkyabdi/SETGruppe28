package no.hiof.gruppe28.epost;
import no.hiof.gruppe28.dto.PameldingKvittering;


public interface EpostTjeneste {
    void sendBekreftelse(String mottaker, PameldingKvittering kvittering);
}
