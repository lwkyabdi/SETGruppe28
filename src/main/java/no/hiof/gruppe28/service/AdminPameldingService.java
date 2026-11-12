package no.hiof.gruppe28.service;

import no.hiof.gruppe28.dto.AdminOversikt;
import no.hiof.gruppe28.dto.InnloggetBruker;
import no.hiof.gruppe28.epost.EpostTjeneste;
import no.hiof.gruppe28.model.AdminPamelding;
import no.hiof.gruppe28.repository.AdminPameldingRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AdminPameldingService {

    private final AdminPameldingRepository repository;
    private final EpostTjeneste epost;

    public AdminPameldingService(AdminPameldingRepository repository) {
        this(repository, null);
    }

    public AdminPameldingService(AdminPameldingRepository repository, EpostTjeneste epost) {
        this.repository = repository;
        this.epost = epost;
    }

    public AdminOversikt oversikt(InnloggetBruker bruker) {
        Long lag = lagFilter(bruker);
        List<AdminPamelding> alle = repository.finnPameldinger(lag, null);

        int pameldte = (int) alle.stream().filter(p -> "PAMELDT".equals(p.status())).count();
        int venteliste = (int) alle.stream().filter(p -> "VENTELISTE".equals(p.status())).count();

        return new AdminOversikt(repository.antallKurs(lag), pameldte, venteliste);
    }

    // Påmeldte først, så ventelisten i den rekkefølgen de meldte seg på
    public List<AdminPamelding> pameldinger(InnloggetBruker bruker, Long kursId) {
        return repository.finnPameldinger(lagFilter(bruker), kursId).stream()
            .sorted(Comparator
                .comparing((AdminPamelding p) -> "PAMELDT".equals(p.status()) ? 0 : 1)
                .thenComparing(AdminPamelding::opprettet,
                    Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    public void giPlass(InnloggetBruker bruker, String referanse) {
        AdminPamelding pamelding = finnEgen(bruker, referanse);

        if ("PAMELDT".equals(pamelding.status())) {
            throw new ApiException(409, "Personen er allerede påmeldt.");
        }
        if (!repository.giPlass(referanse)) {
            throw new ApiException(409, "Kurset er fullt. Det er ingen ledig plass å gi.");
        }

        sendPlassEpost(pamelding);
    }

    public void slett(InnloggetBruker bruker, String referanse) {
        finnEgen(bruker, referanse);
        repository.slett(referanse);
    }


    public void kontrollerKurs(InnloggetBruker bruker, long kursId) {
        Long kursetsLag = repository.finnLokallagForKurs(kursId)
            .orElseThrow(() -> new ApiException(404, "Kurset finnes ikke."));

        if (!harTilgang(bruker, kursetsLag)) {
            throw new ApiException(404, "Kurset finnes ikke.");
        }
    }


    private void sendPlassEpost(AdminPamelding p) {
        if (epost == null) {
            return;
        }
        String emne = "Du har fått plass på " + p.kursTittel();
        String tekst = "Hei " + p.fornavn() + "!\n\n"
            + "Det har blitt ledig plass, og du er nå påmeldt «" + p.kursTittel() + "».\n\n"
            + "Referansen din er " + p.referanse() + ".\n\n"
            + "Har du spørsmål, ta kontakt med lokallaget.\n\n"
            + "Vennlig hilsen\n"
            + "Østfold Husflidslag";
        try {
            epost.sendMelding(p.epost(), emne, tekst);
        } catch (RuntimeException e) {
            System.getLogger(AdminPameldingService.class.getName())
                .log(System.Logger.Level.WARNING, "Kunne ikke sende e-post om ledig plass.", e);
        }
    }


    private AdminPamelding finnEgen(InnloggetBruker bruker, String referanse) {
        AdminPamelding pamelding = repository.finn(referanse)
            .orElseThrow(() -> new ApiException(404, "Påmeldingen finnes ikke."));

        if (!harTilgang(bruker, pamelding.kursLokallagId())) {
            throw new ApiException(404, "Påmeldingen finnes ikke.");
        }
        return pamelding;
    }


    private Long lagFilter(InnloggetBruker bruker) {
        if ("ADMIN".equals(bruker.rolle())) {
            return null;
        }
        if (bruker.lokallagId() == null) {
            throw new ApiException(403, "Brukeren er ikke knyttet til et lokallag.");
        }
        return bruker.lokallagId();
    }

    private boolean harTilgang(InnloggetBruker bruker, Long kursetsLag) {
        return "ADMIN".equals(bruker.rolle())
            || (bruker.lokallagId() != null && Objects.equals(bruker.lokallagId(), kursetsLag));
    }
}
