package no.hiof.gruppe28.repository;
import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.model.PameldingData;
import no.hiof.gruppe28.model.Registrering;
import no.hiof.gruppe28.service.PameldingException;

import java.time.LocalDate;

public interface PameldingRepository {
    Registrering registrer(
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
    }
}
