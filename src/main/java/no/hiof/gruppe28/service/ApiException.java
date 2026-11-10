package no.hiof.gruppe28.service;

public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String melding) {
        super(melding);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
