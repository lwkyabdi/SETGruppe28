package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.model.Kurs;
import java.math.BigDecimal;

public record KursDto(
        long id, String tittel, String beskrivelse, String dato,
        String tidspunkt, String varighet, String sted, String lokallag,
        String tema, String malgruppe, String niva, String utstyr,
        BigDecimal medlemspris, BigDecimal ordinaerpris,
        int kapasitet, int ledigePlasser, Kontaktperson kontakt) {
    public static KursDto from(Kurs k) {
        return new KursDto(k.id(), k.tittel(), k.beskrivelse(),
                k.dato() == null ? null : k.dato().toString(),
                k.tidspunkt(), k.varighet(), k.sted(), k.lokallag(), k.tema(),
                k.malgruppe(), k.niva(), k.utstyr(), k.medlemspris(),
                k.ordinaerpris(), k.kapasitet(), k.ledigePlasser(), k.kontakt());
    }
}
