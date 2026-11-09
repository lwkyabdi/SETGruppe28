package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.Nyhet;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public record NyhetDto(
    long id,
    String dato,
    String datoTekst,
    String lokallag,
    String tittel,
    String ingress,
    List<String> tekst,
    String bilde
) {
    private static final DateTimeFormatter NORSK_DATO =
        DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.forLanguageTag("nb-NO"));

    public static NyhetDto from(Nyhet n) {
        return new NyhetDto(
            n.id(),
            n.dato().toString(),
            n.dato().format(NORSK_DATO),
            n.lokallag(),
            n.tittel(),
            n.ingress(),
            n.tekst(),
            n.bilde()
        );
    }
}

