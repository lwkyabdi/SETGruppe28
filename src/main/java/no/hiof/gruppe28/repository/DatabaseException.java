package no.hiof.gruppe28.repository;

public class DatabaseException extends RuntimeException {
    public DatabaseException(Throwable cause) {
        super("Databaseoperasjonen feilet.", cause);
    }
}
