package no.hiof.gruppe28.model;

import java.time.LocalDateTime;

public record Artikkel(
    long id,
    String tittel,
    String ingress,
    String innhold,
    String bildesti,
    String bildealt,
    Long lokallagId,
    String lokallagNavn,
    LocalDateTime publisertTid
) { }
