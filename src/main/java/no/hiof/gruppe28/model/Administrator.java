package no.hiof.gruppe28.model;

public record Administrator(
    long id,
    String navn,
    String epost,
    String passordHash,
    String rolle,
    Long lokallagId,
    boolean aktiv
) { }


