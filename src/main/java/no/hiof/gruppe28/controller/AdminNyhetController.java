package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.dto.AdminNyhetDto;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.NyhetSkjema;
import no.hiof.gruppe28.service.AdminNyhetService;
import no.hiof.gruppe28.service.ApiException;

import java.util.Map;

public class AdminNyhetController {

    private final AdminNyhetService service;

    public AdminNyhetController(AdminNyhetService service) {
        this.service = service;
    }


    public void alle(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        ctx.json(service.alle(bruker(ctx)).stream()
            .map(AdminNyhetDto::from)
            .toList());
    }


    public void finn(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        ctx.json(AdminNyhetDto.from(service.finn(bruker(ctx), id(ctx))));
    }


    public void opprett(Context ctx) {
        long id = service.opprett(bruker(ctx), skjema(ctx));
        ctx.status(201).json(Map.of("id", id, "melding", "Nyheten er opprettet."));
    }


    public void oppdater(Context ctx) {
        service.oppdater(bruker(ctx), id(ctx), skjema(ctx));
        ctx.json(Map.of("melding", "Nyheten er lagret."));
    }


    public void slett(Context ctx) {
        service.slett(bruker(ctx), id(ctx));
        ctx.json(Map.of("melding", "Nyheten er slettet."));
    }

    private NyhetSkjema skjema(Context ctx) {
        try {
            return ctx.bodyAsClass(NyhetSkjema.class);
        } catch (Exception e) {
            throw new ApiException(400, "Nyheten må sendes som gyldig JSON.");
        }
    }

    private long id(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException e) {

        }
        throw new ApiException(400, "Nyhet-ID må være et positivt heltall.");
    }

    private InnloggetBruker bruker(Context ctx) {
        InnloggetBruker bruker = ctx.sessionAttribute(InnloggingController.SESJON_NOKKEL);
        if (bruker == null) {
            throw new IkkeInnloggetException();
        }
        return bruker;
    }
}
