package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.model.PameldingStatus;

import java.math.BigDecimal;

public record PameldingKvittering(
        String referanse,
        long kursId,
        String kursTittel,
        String dato,
        String tidspunkt,
        String sted,
        String lokallag,
        BigDecimal pris,
        int ledigePlasser,
        boolean demo,
        String status
) {
    public static PameldingKvittering fra(
            Kurs kurs,
            String referanse,
            BigDecimal pris,
            boolean demo,
            PameldingStatus status
    ) {
        return new PameldingKvittering(
                referanse,
                kurs.id(),
                kurs.tittel(),
                kurs.dato().toString(),
                kurs.tidspunkt(),
                kurs.sted(),
                kurs.lokallag(),
                pris,
                kurs.ledigePlasser(),
                demo,
            status.name().toLowerCase()
        );
    }
}
