package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.dto.AdminPameldingDto;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.service.AdminPameldingService;
import no.hiof.gruppe28.service.ApiException;

import java.util.Map;

public class AdminPameldingController {

    private final AdminPameldingService service;
    private final PameldingController pameldingController;

    public AdminPameldingController(AdminPameldingService service, PameldingController pameldingController) {
        this.service = service;
        this.pameldingController = pameldingController;
    }

    // GET /api/admin/oversikt
    public void oversikt(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        ctx.json(service.oversikt(bruker(ctx)));
    }

    // GET /api/admin/pameldinger?kurs={id}  (uten kurs = alle egne kurs)
    public void pameldinger(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        Long kursId = null;
        String kurs = ctx.queryParam("kurs");
        if (kurs != null && !kurs.isBlank()) {
            kursId = positivtTall(kurs, "Kurs-ID må være et positivt heltall.");
        }

        ctx.json(service.pameldinger(bruker(ctx), kursId).stream()
            .map(AdminPameldingDto::from)
            .toList());
    }

    // POST /api/admin/pameldinger/{referanse}/gi-plass
    public void giPlass(Context ctx) {
        service.giPlass(bruker(ctx), ctx.pathParam("referanse"));
        ctx.json(Map.of("melding", "Personen har fått plass på kurset."));
    }

    // DELETE /api/admin/pameldinger/{referanse}
    public void slett(Context ctx) {
        service.slett(bruker(ctx), ctx.pathParam("referanse"));
        ctx.json(Map.of("melding", "Påmeldingen er slettet."));
    }

    // POST /api/admin/kurs/{id}/pameldinger
    public void meldPa(Context ctx) {
        long kursId = positivtTall(ctx.pathParam("id"), "Kurs-ID må være et positivt heltall.");
        service.kontrollerKurs(bruker(ctx), kursId);
        pameldingController.registrer(ctx);
    }

    // Den innloggede brukeren. Vakten har allerede sjekket at den finnes.
    private InnloggetBruker bruker(Context ctx) {
        InnloggetBruker bruker = ctx.sessionAttribute(InnloggingController.SESJON_NOKKEL);
        if (bruker == null) {
            throw new IkkeInnloggetException();
        }
        return bruker;
    }

    private long positivtTall(String tekst, String feilmelding) {
        try {
            long tall = Long.parseLong(tekst);
            if (tall > 0) {
                return tall;
            }
        } catch (NumberFormatException e) {
            // faller ned til feilen under
        }
        throw new ApiException(400, feilmelding);
    }
}
