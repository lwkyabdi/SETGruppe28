package no.hiof.gruppe28.controller;

import no.hiof.gruppe28.service.ApiException;

public class IkkeInnloggetException extends ApiException {

    public IkkeInnloggetException() {
        super(401, "Du er ikke logget inn.");
    }
}

