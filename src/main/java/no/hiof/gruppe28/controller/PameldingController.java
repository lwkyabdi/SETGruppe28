package no.hiof.gruppe28.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import no.hiof.gruppe28.dto.PameldingRequest;
import no.hiof.gruppe28.service.PameldingException;
import no.hiof.gruppe28.service.PameldingService;

public class PameldingController {

    private final PameldingService service;

    private final ObjectMapper json = new ObjectMapper()
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

    public PameldingController(PameldingService service) {
        this.service = service;
    }

    public void registrer(Context ctx) {
        ctx.header("Cache-Control", "no-store");

        long kursId;

        try {
            kursId = Long.parseLong(ctx.pathParam("id"));

            if (kursId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new PameldingException(
                    400,
                    "Kurs-ID må være et positivt heltall."
            );
        }

        String body = ctx.body();

        if (body.length() > 16000) {
            throw new PameldingException(
                    413,
                    "Påmeldingen er for stor."
            );
        }

        PameldingRequest request;

        try {
            request = json.readValue(body, PameldingRequest.class);
        } catch (JsonProcessingException e) {
            throw new PameldingException(
                    400,
                    "Påmeldingen må inneholde gyldig JSON med riktige felt."
            );
        }

        var kvittering = service.registrer(kursId, request);

        ctx.status(201).json(kvittering);
    }
}