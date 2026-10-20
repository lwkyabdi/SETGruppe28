package no.hiof.gruppe28.service;

import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.repository.KursRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KursService {
    private final KursRepository repository;
    private final Clock clock;

    public KursService(KursRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Kurs> allePubliserte() { return repository.findPubliserte(); }
    public Optional<Kurs> finnPublisert(long id) { return repository.findPublisert(id); }

    public List<Kurs> kommende() {
        return getKommendeKurs(repository.findPubliserte(), LocalDate.now(clock));
    }

    public static List<Kurs> getKommendeKurs(List<Kurs> kurs, LocalDate idag) {
        Objects.requireNonNull(idag, "Dagens dato må være oppgitt.");
        if (kurs == null) return List.of();
        return kurs.stream()
                .filter(Objects::nonNull)
                .filter(k -> k.dato() != null && !k.dato().isBefore(idag))
                .sorted(Comparator.comparing(Kurs::dato).thenComparingLong(Kurs::id))
                .toList();
    }
}
