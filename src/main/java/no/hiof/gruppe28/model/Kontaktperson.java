package no.hiof.gruppe28.model;

public record Kontaktperson(
    long id,
    String navn,
    String epost,
    String telefon,
    String bilde,
    String verv,
    String sted
) {

    public Kontaktperson(long id, String navn, String epost, String telefon, String bilde) {
        this(id, navn, epost, telefon, bilde, null, null);
    }
}
