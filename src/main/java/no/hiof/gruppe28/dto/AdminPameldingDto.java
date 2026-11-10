package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.AdminPamelding;

import java.util.Locale;

public record AdminPameldingDto(
    String referanse,
    long kursId,
    String kursTittel,
    String navn,
    String epost,
    String telefon,
    boolean medlem,
    String status,
    String opprettet
) {
    public static AdminPameldingDto from(AdminPamelding p) {
        return new AdminPameldingDto(
            p.referanse(),
            p.kursId(),
            p.kursTittel(),
            p.fornavn() + " " + p.etternavn(),
            p.epost(),
            p.telefon(),
            p.medlem(),
            p.status().toLowerCase(Locale.ROOT),
            p.opprettet() == null ? null : p.opprettet().toString()
        );
    }
}
