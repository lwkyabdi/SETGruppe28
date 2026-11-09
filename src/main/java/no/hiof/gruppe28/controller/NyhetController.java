package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.dto.NyhetDto;
import no.hiof.gruppe28.service.NyhetService;

import java.util.Map;

public class NyhetController {

    private final NyhetService service;

    public NyhetController(NyhetService service) {
        this.service = service;
    }

    public void alle(Context ctx) {
        ctx.json(service.alle().stream().map(NyhetDto::from).toList());
    }

    public void detalj(Context ctx) {
        long id;
        try {
            id = Long.parseLong(ctx.pathParam("id"));
            if (id <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("melding", "Nyhet-ID må være et positivt heltall."));
            return;
        }

        service.finn(id).ifPresentOrElse(
            nyhet -> ctx.json(NyhetDto.from(nyhet)),
            () -> ctx.status(404).json(Map.of("melding", "Nyheten finnes ikke.")));
    }
}
