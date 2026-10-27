package no.hiof.gruppe28.service;

public class PameldingException extends RuntimeException {
    private final int status;

    public PameldingException(int status, String melding) {
        super(melding);
        this.status = status;
    }

    public int status() {
        return status;
    }
}