package no.hiof.gruppe28.dto;

public record NyhetSkjema(
    String tittel,
    String ingress,
    String innhold,
    String bildesti,
    String bildealt,
    Long lokallagId,
    String status,
    String dato
) { }
