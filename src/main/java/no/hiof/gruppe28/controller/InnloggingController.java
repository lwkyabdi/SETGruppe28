package no.hiof.gruppe28.controller;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpSession;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.InnloggingForesporsel;
import no.hiof.gruppe28.service.InnloggingService;

import java.util.Map;

public class InnloggingController {

    public static final String SESJON_NOKKEL = "innlogget";

    private final InnloggingService service;

    public InnloggingController(InnloggingService service) {
        this.service = service;
    }

    public void loggInn(Context ctx) {
        InnloggingForesporsel foresporsel;
        try {
            foresporsel = ctx.bodyAsClass(InnloggingForesporsel.class);
        } catch (Exception e) {
            ctx.status(400).json(Map.of("melding", "Ugyldig forespørsel."));
            return;
        }

        if (foresporsel == null || erTom(foresporsel.epost()) || erTom(foresporsel.passord())) {
            ctx.status(400).json(Map.of("melding", "E-post og passord må fylles ut."));
            return;
        }

        service.loggInn(foresporsel.epost(), foresporsel.passord()).ifPresentOrElse(
            admin -> {
                InnloggetBruker bruker = InnloggetBruker.from(admin);
                ctx.req().getSession(true);
                ctx.req().changeSessionId();
                ctx.sessionAttribute(SESJON_NOKKEL, bruker);
                ctx.json(bruker);
            },
            () -> ctx.status(401).json(Map.of("melding", "Feil e-post eller passord.")));
    }

    public void loggUt(Context ctx) {
        HttpSession sesjon = ctx.req().getSession(false);
        if (sesjon != null) {
            sesjon.invalidate();
        }
        ctx.json(Map.of("melding", "Du er logget ut."));
    }

    public void meg(Context ctx) {
        InnloggetBruker bruker = ctx.sessionAttribute(SESJON_NOKKEL);
        if (bruker == null) {
            ctx.status(401).json(Map.of("melding", "Du er ikke logget inn."));
            return;
        }
        ctx.json(bruker);
    }

    public void krevInnlogging(Context ctx) {
        InnloggetBruker bruker = ctx.sessionAttribute(SESJON_NOKKEL);
        if (bruker == null) {
            throw new IkkeInnloggetException();
        }
    }

    private boolean erTom(String tekst) {
        return tekst == null || tekst.isBlank();
    }
}
