package no.hiof.gruppe28.service;

import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.dto.PameldingRequest;
import no.hiof.gruppe28.model.PameldingData;
import no.hiof.gruppe28.repository.PameldingRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

public class PameldingService {
    private final PameldingRepository repository;
    private final Clock clock;

    public PameldingService(PameldingRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public boolean erDemo() {
        return repository.erDemo();
    }

    public PameldingKvittering registrer(long kursId, PameldingRequest r) {
        if (kursId <= 0 || r == null) {
            throw feil("Ugyldig kurs eller påmelding.");
        }

        String referanse;

        try {
            referanse = UUID.fromString(r.nokkel()).toString();

            if (!referanse.equalsIgnoreCase(r.nokkel())) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw feil("Påmeldingsnøkkelen mangler eller er ugyldig.");
        }

        String fornavn = tekst(r.fornavn(), "Fornavn", 80, true);
        String etternavn = tekst(r.etternavn(), "Etternavn", 80, true);

        String epost = tekst(r.epost(), "E-post", 150, true)
                .toLowerCase(Locale.ROOT);

        if (!epost.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw feil("Oppgi en gyldig e-postadresse.");
        }

        String telefon = tekst(r.telefon(), "Telefon", 20, true);

        if (!telefon.matches("\\+?[0-9][0-9 ]{7,18}")
                || !telefon.replaceAll("[+ ]", "").matches("[0-9]{8,15}")) {
            throw feil("Oppgi et telefonnummer med 8–15 sifre.");
        }

        LocalDate idag = LocalDate.now(clock);

        if (r.fodselsar() == null
                || r.fodselsar() < 1900
                || r.fodselsar() > idag.getYear()) {
            throw feil("Oppgi et gyldig fødselsår.");
        }

        if (!"ja".equals(r.medlem()) && !"nei".equals(r.medlem())) {
            throw feil("Velg medlemskap.");
        }

        if (!Boolean.TRUE.equals(r.samtykke())) {
            throw feil("Samtykke er nødvendig for å behandle påmeldingen.");
        }

        boolean medlem = "ja".equals(r.medlem());

        String medlemsnummer = tekst(
                r.medlemsnummer(), "Medlemsnummer", 40, false
        );

        if (!medlem) {
            medlemsnummer = "";
        }

        String kommentar = tekst(
                r.kommentar(), "Kommentar", 2000, false
        );

        StringBuilder innhold = new StringBuilder();

        for (String verdi : new String[]{
                Long.toString(kursId),
                fornavn,
                etternavn,
                epost,
                telefon,
                r.fodselsar().toString(),
                r.medlem(),
                medlemsnummer,
                kommentar
        }) {
            innhold.append(verdi.length()).append(':').append(verdi);
        }

        PameldingData data = new PameldingData(
                referanse,
                sha256(innhold.toString()),
                fornavn,
                etternavn,
                epost,
                telefon,
                r.fodselsar(),
                medlem,
                medlemsnummer,
                kommentar
        );

        return repository.registrer(kursId, data, idag);
    }

    private static String tekst(
            String verdi, String navn, int maks, boolean obligatorisk
    ) {
        String v = verdi == null ? "" : verdi.trim();

        if (obligatorisk && v.isEmpty()) {
            throw feil(navn + " må fylles ut.");
        }

        if (v.length() > maks || v.indexOf('\0') >= 0) {
            throw feil(navn + " er for langt eller inneholder ugyldige tegn.");
        }

        return v;
    }

    private static String sha256(String verdi) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(verdi.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PameldingException feil(String melding) {
        return new PameldingException(400, melding);
    }
}