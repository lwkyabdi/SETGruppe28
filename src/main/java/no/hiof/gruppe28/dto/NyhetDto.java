package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.Artikkel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record NyhetDto(
    long id,
    String tittel,
    String ingress,
    String innhold,
    String bildesti,
    String bildealt,
    Long lokallagId,
    String lokallag,
    String dato,
    String datoTekst
) {
    private static final DateTimeFormatter NORSK_DATO =
        DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.forLanguageTag("nb-NO"));

    private static final String HELE_FYLKET = "Østfold Husflidslag";

    public static NyhetDto from(Artikkel a) {
        LocalDate dato = a.publisertTid().toLocalDate();
        String lokallag = a.lokallagNavn() != null ? a.lokallagNavn() : HELE_FYLKET;

        return new NyhetDto(
            a.id(),
            a.tittel(),
            a.ingress(),
            a.innhold(),
            a.bildesti(),
            a.bildealt(),
            a.lokallagId(),
            lokallag,
            dato.toString(),
            dato.format(NORSK_DATO)
        );
    }
}
