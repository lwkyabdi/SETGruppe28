package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.dto.KursDto;
import no.hiof.gruppe28.service.KursService;
import java.util.Map;

public class KursController {
    private final KursService service;
    public KursController(KursService service) { this.service = service; }

    public void alle(Context ctx) {
        ctx.json(service.allePubliserte().stream().map(KursDto::from).toList());
    }

    public void kommende(Context ctx) {
        ctx.json(service.kommende().stream().map(KursDto::from).toList());
    }

    public void detalj(Context ctx) {
        long id;
        try {
            id = Long.parseLong(ctx.pathParam("id"));
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("melding", "Kurs-ID må være et positivt heltall."));
            return;
        }
        service.finnPublisert(id).ifPresentOrElse(
                k -> ctx.json(KursDto.from(k)),
                () -> ctx.status(404).json(Map.of("melding", "Kurset finnes ikke.")));
    }
}
