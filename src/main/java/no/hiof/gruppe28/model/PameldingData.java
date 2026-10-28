package no.hiof.gruppe28.model;

public record PameldingData(
        String referanse,
        String foresporselHash,
        String fornavn,
        String etternavn,
        String epost,
        String telefon,
        int fodselsar,
        boolean medlem,
        String medlemsnummer,
        String kommentar
) {}
