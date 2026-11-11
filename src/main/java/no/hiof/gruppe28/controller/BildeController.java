package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import no.hiof.gruppe28.service.ApiException;
import no.hiof.gruppe28.service.BildeService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class BildeController {

    private final BildeService service;

    public BildeController(BildeService service) {
        this.service = service;
    }


    public void lastOpp(Context ctx) {
        UploadedFile fil = ctx.uploadedFile("bilde");
        if (fil == null) {
            throw new ApiException(400, "Velg et bilde.");
        }

        try (InputStream innhold = fil.content()) {
            String adresse = service.lagre(fil.size(), innhold);
            ctx.status(201).json(Map.of("bildesti", adresse, "melding", "Bildet er lastet opp."));
        } catch (IOException e) {
            throw new ApiException(400, "Kunne ikke lese bildet.");
        }
    }
}
