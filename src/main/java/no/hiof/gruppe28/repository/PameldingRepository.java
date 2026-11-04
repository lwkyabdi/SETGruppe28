package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.model.PameldingData;
import no.hiof.gruppe28.service.PameldingException;

import java.time.LocalDate;

public interface PameldingRepository {
    PameldingKvittering registrer(
            long kursId,
            PameldingData data,
            LocalDate idag
    );

    default boolean erDemo() {
        return false;
    }

    static void kontrollerKurs(Kurs kurs, LocalDate idag) {
        if (kurs.dato() == null || kurs.dato().isBefore(idag)) {
            throw new PameldingException(
                    409, "Påmelding er stengt for dette kurset."
            );
        }

        if (kurs.ledigePlasser() <= 0) {
            throw new PameldingException(
                    409, "Kurset er fullt. Velg et annet kurs."
            );
        }
    }
}
