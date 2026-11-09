package no.hiof.gruppe28.model;

import java.time.LocalDate;
import java.util.List;

public record Nyhet(
    long id,
    String tittel,
    LocalDate dato,
    String lokallag,
    String ingress,
    List<String> tekst,
    String bilde
) {
}



