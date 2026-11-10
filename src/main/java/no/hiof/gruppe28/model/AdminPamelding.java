package no.hiof.gruppe28.model;

import java.time.LocalDateTime;

public record AdminPamelding(
    String referanse,
    long kursId,
    String kursTittel,
    Long kursLokallagId,
    String fornavn,
    String etternavn,
    String epost,
    String telefon,
    boolean medlem,
    String status,
    LocalDateTime opprettet
) { }


