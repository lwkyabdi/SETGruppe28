package no.hiof.gruppe28.model;

public record Lokallag(
    long id,
    String slug,
    String navn,
    String beskrivelse,
    String adresse,
    String epost,
    String telefon,
    String bildesti
) { }
