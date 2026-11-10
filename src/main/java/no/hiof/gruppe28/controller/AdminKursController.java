package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.dto.AdminKursDto;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.KursSkjema;
import no.hiof.gruppe28.service.AdminKursService;
import no.hiof.gruppe28.service.ApiException;

import java.util.Map;

public class AdminKursController {

    private final AdminKursService service;

    public AdminKursController(AdminKursService service) {
        this.service = service;
    }

    // GET /api/admin/kurs
    public void alle(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        ctx.json(service.alle(bruker(ctx)).stream()
            .map(AdminKursDto::from)
            .toList());
    }

    // GET /api/admin/kurs/{id}
    public void finn(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        ctx.json(AdminKursDto.from(service.finn(bruker(ctx), id(ctx))));
    }

    // POST /api/admin/kurs
    public void opprett(Context ctx) {
        long id = service.opprett(bruker(ctx), skjema(ctx));
        ctx.status(201).json(Map.of("id", id, "melding", "Kurset er opprettet."));
    }

    // PUT /api/admin/kurs/{id}
    public void oppdater(Context ctx) {
        service.oppdater(bruker(ctx), id(ctx), skjema(ctx));
        ctx.json(Map.of("melding", "Kurset er lagret."));
    }

    // DELETE /api/admin/kurs/{id}
    public void slett(Context ctx) {
        service.slett(bruker(ctx), id(ctx));
        ctx.json(Map.of("melding", "Kurset er slettet."));
    }

    private KursSkjema skjema(Context ctx) {
        try {
            return ctx.bodyAsClass(KursSkjema.class);
        } catch (Exception e) {
            throw new ApiException(400, "Kurset må sendes som gyldig JSON.");
        }
    }

    private long id(Context ctx) {
        try {
            long id = Long.parseLong(ctx.pathParam("id"));
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException e) {
            // faller ned til feilen under
        }
        throw new ApiException(400, "Kurs-ID må være et positivt heltall.");
    }

    private InnloggetBruker bruker(Context ctx) {
        InnloggetBruker bruker = ctx.sessionAttribute(InnloggingController.SESJON_NOKKEL);
        if (bruker == null) {
            throw new IkkeInnloggetException();
        }
        return bruker;
    }
}

