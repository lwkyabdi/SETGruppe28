package no.hiof.gruppe28.dto;

import no.hiof.gruppe28.model.Administrator;

public record InnloggetBruker(long id, String navn, String epost, String rolle, Long lokallagId) {

    public static InnloggetBruker from(Administrator admin) {
        return new InnloggetBruker(admin.id(), admin.navn(), admin.epost(), admin.rolle(), admin.lokallagId());
    }
}
