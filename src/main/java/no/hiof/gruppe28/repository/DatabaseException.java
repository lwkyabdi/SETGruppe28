package no.hiof.gruppe28.repository;

public class DatabaseException extends RuntimeException {
    public DatabaseException(Throwable cause) {
        super("Kunne ikke lese kurs fra databasen.", cause);
    }
}
