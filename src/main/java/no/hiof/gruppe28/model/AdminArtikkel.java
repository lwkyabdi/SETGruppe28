package no.hiof.gruppe28.model;

import java.time.LocalDateTime;

public record AdminArtikkel(
    long id,
    String tittel,
    String ingress,
    String innhold,
    String bildesti,
    String bildealt,
    Long lokallagId,
    String lokallagNavn,
    String status,
    LocalDateTime publisertTid
) { }
