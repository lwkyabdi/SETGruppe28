package no.hiof.gruppe28.dto;

public record PameldingRequest(
        String nokkel,
        String fornavn,
        String etternavn,
        String epost,
        String telefon,
        Integer fodselsar,
        String medlem,
        String medlemsnummer,
        String kommentar,
        Boolean samtykke
) {}
