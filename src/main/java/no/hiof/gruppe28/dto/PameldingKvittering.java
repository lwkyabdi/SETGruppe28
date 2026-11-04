package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.Kurs;
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
        boolean demo
) {
    public static PameldingKvittering fra(
            Kurs kurs,
            String referanse,
            BigDecimal pris,
            boolean demo
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
                demo
        );
    }
}