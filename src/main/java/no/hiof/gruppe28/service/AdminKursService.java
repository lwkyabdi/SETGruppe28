package no.hiof.gruppe28.service;

import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.KursSkjema;
import no.hiof.gruppe28.model.AdminKurs;
import no.hiof.gruppe28.model.KursData;
import no.hiof.gruppe28.repository.AdminKursRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AdminKursService {

    private final AdminKursRepository repository;

    public AdminKursService(AdminKursRepository repository) {
        this.repository = repository;
    }

    // Egne kurs, også kladder, sortert etter dato (kurs uten dato sist)
    public List<AdminKurs> alle(InnloggetBruker bruker) {
        return repository.finnAlle(lagFilter(bruker)).stream()
            .sorted(Comparator.comparing(AdminKurs::dato, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingLong(AdminKurs::id))
            .toList();
    }

    public AdminKurs finn(InnloggetBruker bruker, long id) {
        return finnEget(bruker, id);
    }

    public long opprett(InnloggetBruker bruker, KursSkjema skjema) {
        return repository.opprett(valider(bruker, skjema));
    }

    public void oppdater(InnloggetBruker bruker, long id, KursSkjema skjema) {
        finnEget(bruker, id);
        if (!repository.oppdater(id, valider(bruker, skjema))) {
            throw new ApiException(409, "Kapasiteten kan ikke være lavere enn antall påmeldte.");
        }
    }

    public void slett(InnloggetBruker bruker, long id) {
        finnEget(bruker, id);
        if (!repository.slettHvisIngenPameldte(id)) {
            throw new ApiException(409, "Kurset har påmeldte eller venteliste og kan ikke slettes.");
        }
    }

    // Sjekker alle feltene og gjør skjemaet om til det som lagres
    private KursData valider(InnloggetBruker bruker, KursSkjema s) {
        if (s == null) {
            throw new ApiException(400, "Kurset mangler opplysninger.");
        }

        LocalDate dato;
        try {
            dato = LocalDate.parse(tekst(s.dato(), "Dato", 10));
        } catch (DateTimeParseException e) {
            throw new ApiException(400, "Dato må skrives som ÅÅÅÅ-MM-DD.");
        }

        if (s.kapasitet() == null || s.kapasitet() < 1 || s.kapasitet() > 1000) {
            throw new ApiException(400, "Antall plasser må være mellom 1 og 1000.");
        }

        String status = tekst(s.status(), "Status", 20);
        if (!status.equals("KLADD") && !status.equals("PUBLISERT")) {
            throw new ApiException(400, "Status må være KLADD eller PUBLISERT.");
        }

        if (s.kontaktId() == null || !repository.kontaktpersonFinnes(s.kontaktId())) {
            throw new ApiException(400, "Velg en kontaktperson som finnes.");
        }

        return new KursData(
            tekst(s.tittel(), "Tittel", 200),
            tekst(s.beskrivelse(), "Beskrivelse", 5000),
            dato,
            tekst(s.tidspunkt(), "Tidspunkt", 100),
            tekst(s.varighet(), "Varighet", 100),
            tekst(s.sted(), "Sted", 200),
            riktigLag(bruker, s.lokallagId()),
            tekst(s.tema(), "Tema", 100),
            tekst(s.malgruppe(), "Målgruppe", 100),
            tekst(s.niva(), "Nivå", 100),
            tekst(s.utstyr(), "Utstyr", 2000),
            pris(s.medlemspris(), "Medlemspris"),
            pris(s.ordinaerpris(), "Ordinær pris"),
            s.kapasitet(),
            status,
            s.kontaktId(),
            valgfri(s.bildesti(), "Bildesti", 300),
            valgfri(s.bildealt(), "Bildetekst", 300)
        );
    }

    // LOKALLAG lager alltid kurs for sitt eget lag. ADMIN må velge et lag som finnes.
    private Long riktigLag(InnloggetBruker bruker, Long valgt) {
        if (!"ADMIN".equals(bruker.rolle())) {
            Long eget = lagFilter(bruker);
            if (valgt != null && !valgt.equals(eget)) {
                throw new ApiException(400, "Du kan bare lage kurs for ditt eget lokallag.");
            }
            return eget;
        }
        if (valgt == null || !repository.lokallagFinnes(valgt)) {
            throw new ApiException(400, "Velg et lokallag som finnes.");
        }
        return valgt;
    }

    private AdminKurs finnEget(InnloggetBruker bruker, long id) {
        AdminKurs kurs = repository.finn(id)
            .orElseThrow(() -> new ApiException(404, "Kurset finnes ikke."));
        if (!harTilgang(bruker, kurs.lokallagId())) {
            throw new ApiException(404, "Kurset finnes ikke.");
        }
        return kurs;
    }

    private static String tekst(String verdi, String navn, int maks) {
        String v = verdi == null ? "" : verdi.trim();
        if (v.isEmpty()) {
            throw new ApiException(400, navn + " må fylles ut.");
        }
        if (v.length() > maks) {
            throw new ApiException(400, navn + " kan ha maks " + maks + " tegn.");
        }
        return v;
    }

    private static String valgfri(String verdi, String navn, int maks) {
        String v = verdi == null ? "" : verdi.trim();
        if (v.length() > maks) {
            throw new ApiException(400, navn + " kan ha maks " + maks + " tegn.");
        }
        return v;
    }

    private static BigDecimal pris(BigDecimal verdi, String navn) {
        if (verdi == null || verdi.signum() < 0) {
            throw new ApiException(400, navn + " må fylles ut og kan ikke være negativ.");
        }
        return verdi;
    }

    // ADMIN ser alle lag (null). LOKALLAG ser bare sitt eget.
    private static Long lagFilter(InnloggetBruker bruker) {
        if ("ADMIN".equals(bruker.rolle())) {
            return null;
        }
        if (bruker.lokallagId() == null) {
            throw new ApiException(403, "Brukeren er ikke knyttet til et lokallag.");
        }
        return bruker.lokallagId();
    }

    private static boolean harTilgang(InnloggetBruker bruker, Long kursetsLag) {
        return "ADMIN".equals(bruker.rolle())
            || (bruker.lokallagId() != null && Objects.equals(bruker.lokallagId(), kursetsLag));
    }
}

