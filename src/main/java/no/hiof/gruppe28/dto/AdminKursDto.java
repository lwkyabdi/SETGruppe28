package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.AdminKurs;

import java.math.BigDecimal;

public record AdminKursDto(
    long id,
    String tittel,
    String beskrivelse,
    String dato,
    String tidspunkt,
    String varighet,
    String sted,
    Long lokallagId,
    String lokallag,
    String tema,
    String malgruppe,
    String niva,
    String utstyr,
    BigDecimal medlemspris,
    BigDecimal ordinaerpris,
    int kapasitet,
    int ledigePlasser,
    String status,
    long kontaktId,
    String bildesti,
    String bildealt
) {
    public static AdminKursDto from(AdminKurs k) {
        return new AdminKursDto(
            k.id(), k.tittel(), k.beskrivelse(),
            k.dato() == null ? null : k.dato().toString(),
            k.tidspunkt(), k.varighet(), k.sted(),
            k.lokallagId(), k.lokallagNavn(),
            k.tema(), k.malgruppe(), k.niva(), k.utstyr(),
            k.medlemspris(), k.ordinaerpris(),
            k.kapasitet(), k.ledigePlasser(),
            k.status(), k.kontaktId(),
            k.bildesti(), k.bildealt()
        );
    }
}
