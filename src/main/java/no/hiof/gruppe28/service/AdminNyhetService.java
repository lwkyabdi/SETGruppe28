package no.hiof.gruppe28.service;

import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.dto.NyhetSkjema;
import no.hiof.gruppe28.model.AdminArtikkel;
import no.hiof.gruppe28.model.ArtikkelData;
import no.hiof.gruppe28.repository.AdminArtikkelRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AdminNyhetService {

    private final AdminArtikkelRepository repository;
    private final Clock clock;

    public AdminNyhetService(AdminArtikkelRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }


    public List<AdminArtikkel> alle(InnloggetBruker bruker) {
        return repository.finnAlle(lagFilter(bruker)).stream()
            .sorted(Comparator.comparing(AdminArtikkel::publisertTid,
                    Comparator.nullsFirst(Comparator.<LocalDateTime>reverseOrder()))
                .thenComparing(AdminArtikkel::id, Comparator.reverseOrder()))
            .toList();
    }

    public AdminArtikkel finn(InnloggetBruker bruker, long id) {
        return finnEgen(bruker, id);
    }

    public long opprett(InnloggetBruker bruker, NyhetSkjema skjema) {
        return repository.opprett(valider(bruker, skjema, null));
    }

    public void oppdater(InnloggetBruker bruker, long id, NyhetSkjema skjema) {
        AdminArtikkel eksisterende = finnEgen(bruker, id);
        if (!repository.oppdater(id, valider(bruker, skjema, eksisterende.publisertTid()))) {
            throw new ApiException(404, "Nyheten finnes ikke.");
        }
    }

    public void slett(InnloggetBruker bruker, long id) {
        finnEgen(bruker, id);
        if (!repository.slett(id)) {
            throw new ApiException(404, "Nyheten finnes ikke.");
        }
    }


    private ArtikkelData valider(InnloggetBruker bruker, NyhetSkjema s, LocalDateTime tidligereTid) {
        if (s == null) {
            throw new ApiException(400, "Nyheten mangler opplysninger.");
        }

        String status = tekst(s.status(), "Status", 20);
        if (!status.equals("KLADD") && !status.equals("PUBLISERT")) {
            throw new ApiException(400, "Status må være KLADD eller PUBLISERT.");
        }

        return new ArtikkelData(
            tekst(s.tittel(), "Tittel", 200),
            tekst(s.ingress(), "Ingress", 500),
            tekst(s.innhold(), "Innhold", 20000),
            valgfri(s.bildesti(), "Bildesti", 300),
            valgfri(s.bildealt(), "Bildetekst", 300),
            riktigLag(bruker, s.lokallagId()),
            status,
            publiseringstid(s.dato(), status, tidligereTid)
        );
    }


    private LocalDateTime publiseringstid(String dato, String status, LocalDateTime tidligereTid) {
        if (dato != null && !dato.isBlank()) {
            try {
                return LocalDate.parse(dato.trim()).atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new ApiException(400, "Dato må skrives som ÅÅÅÅ-MM-DD.");
            }
        }
        if (tidligereTid != null) {
            return tidligereTid;
        }
        return status.equals("PUBLISERT") ? LocalDateTime.now(clock) : null;
    }


    private Long riktigLag(InnloggetBruker bruker, Long valgt) {
        if (!"ADMIN".equals(bruker.rolle())) {
            Long eget = lagFilter(bruker);
            if (valgt != null && !valgt.equals(eget)) {
                throw new ApiException(400, "Du kan bare skrive nyheter for ditt eget lokallag.");
            }
            return eget;
        }
        if (valgt != null && !repository.lokallagFinnes(valgt)) {
            throw new ApiException(400, "Velg et lokallag som finnes.");
        }
        return valgt;
    }

    private AdminArtikkel finnEgen(InnloggetBruker bruker, long id) {
        AdminArtikkel artikkel = repository.finn(id)
            .orElseThrow(() -> new ApiException(404, "Nyheten finnes ikke."));
        if (!harTilgang(bruker, artikkel.lokallagId())) {
            throw new ApiException(404, "Nyheten finnes ikke.");
        }
        return artikkel;
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


    private static Long lagFilter(InnloggetBruker bruker) {
        if ("ADMIN".equals(bruker.rolle())) {
            return null;
        }
        if (bruker.lokallagId() == null) {
            throw new ApiException(403, "Brukeren er ikke knyttet til et lokallag.");
        }
        return bruker.lokallagId();
    }

    private static boolean harTilgang(InnloggetBruker bruker, Long artikkelensLag) {
        return "ADMIN".equals(bruker.rolle())
            || (bruker.lokallagId() != null && Objects.equals(bruker.lokallagId(), artikkelensLag));
    }
}
