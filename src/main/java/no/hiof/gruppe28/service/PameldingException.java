package no.hiof.gruppe28.service;

public class PameldingException extends ApiException {

    public PameldingException(int status, String melding) {
        super(status, melding);
    }
}
