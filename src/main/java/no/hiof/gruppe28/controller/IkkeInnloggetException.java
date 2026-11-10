package no.hiof.gruppe28.controller;

public class IkkeInnloggetException extends RuntimeException {

    public IkkeInnloggetException() {
        super("Du er ikke logget inn.");
    }
}


