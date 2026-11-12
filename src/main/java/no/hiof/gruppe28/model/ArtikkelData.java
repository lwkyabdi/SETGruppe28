package no.hiof.gruppe28.model;

import java.time.LocalDateTime;

public record ArtikkelData(
    String tittel,
    String ingress,
    String innhold,
    String bildesti,
    String bildealt,
    Long lokallagId,
    String status,
    LocalDateTime publisertTid
) { }

