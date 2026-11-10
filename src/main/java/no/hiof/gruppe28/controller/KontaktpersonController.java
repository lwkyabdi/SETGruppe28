package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.service.KontaktpersonService;

public class KontaktpersonController {

    private final KontaktpersonService service;

    public KontaktpersonController(KontaktpersonService service) {
        this.service = service;
    }

    public void forKontaktside(Context ctx) {
        ctx.json(service.forKontaktside());
    }
}
