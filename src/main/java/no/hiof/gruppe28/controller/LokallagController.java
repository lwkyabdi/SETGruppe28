package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import no.hiof.gruppe28.service.LokallagService;

public class LokallagController {

    private final LokallagService service;

    public LokallagController(LokallagService service) {
        this.service = service;
    }

    public void alle(Context ctx) {
        ctx.json(service.alle());
    }
}


