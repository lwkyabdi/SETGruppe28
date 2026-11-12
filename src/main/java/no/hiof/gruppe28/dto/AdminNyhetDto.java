package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.AdminArtikkel;

public record AdminNyhetDto(
    long id,
    String tittel,
    String ingress,
    String innhold,
    String bildesti,
    String bildealt,
    Long lokallagId,
    String lokallag,
    String status,
    String dato
) {
    public static AdminNyhetDto from(AdminArtikkel a) {
        return new AdminNyhetDto(
            a.id(),
            a.tittel(),
            a.ingress(),
            a.innhold(),
            a.bildesti(),
            a.bildealt(),
            a.lokallagId(),
            a.lokallagNavn() != null ? a.lokallagNavn() : "Østfold Husflidslag",
            a.status(),
            a.publisertTid() == null ? null : a.publisertTid().toLocalDate().toString()
        );
    }
}
