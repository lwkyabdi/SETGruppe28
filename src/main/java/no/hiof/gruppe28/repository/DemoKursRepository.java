package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.model.Kurs;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DemoKursRepository implements KursRepository {
    private final List<Kurs> kurs;

    public DemoKursRepository(LocalDate idag) {
        Kontaktperson fredrikstad = kontakt(1, "Fredrikstad");
        Kontaktperson sarpsborg = kontakt(2, "Sarpsborg");
        Kontaktperson moss = kontakt(3, "Moss");
        Kontaktperson halden = kontakt(4, "Halden");
        kurs = List.of(
                eksempel(1, "Bunadssøm for nybegynnere", "Lær grunnleggende håndsøm.",
                        idag.plusDays(14), "Fredrikstad", "Bunad", "1500.00", "2000.00", 3, fredrikstad),
                eksempel(2, "Strikk din egen genser", "Lær å lese oppskrift og strikke en enkel genser.",
                        idag.plusDays(21), "Sarpsborg", "Strikking", "1200.00", "1600.00", 0, sarpsborg),
                eksempel(3, "Grunnleggende båndveving", "En innføring i enkel båndveving.",
                        idag.plusDays(28), "Moss", "Veving", "900.00", "1200.00", 6, moss),
                eksempel(4, "Broderi og korssting", "Lær enkle sting og lag et lite broderi.",
                        idag.plusDays(35), "Halden", "Brodering", "800.00", "1100.00", 8, halden),
                eksempel(5, "Tidligere strikkekurs", "Eksempel på et kurs som allerede har vært.",
                        idag.minusDays(7), "Moss", "Strikking", "500.00", "700.00", 0, moss)
        ).stream().sorted(Comparator.comparing(Kurs::dato).thenComparingLong(Kurs::id)).toList();
    }

    @Override
    public List<Kurs> findPubliserte() {
        return kurs;
    }

    @Override
    public Optional<Kurs> findPublisert(long id) {
        return kurs.stream().filter(k -> k.id() == id).findFirst();
    }

    private static Kontaktperson kontakt(long id, String lokallag) {
        return new Kontaktperson(id, "Kurskontakt " + lokallag + " (demo)",
                lokallag.toLowerCase(java.util.Locale.ROOT) + "@example.test", "00000000",
                "/bilder/kontakt-placeholder.svg");
    }

    private static Kurs eksempel(long id, String tittel, String beskrivelse, LocalDate dato,
                                String lokallag, String tema, String medlemspris,
                                String ordinaerpris, int ledigePlasser, Kontaktperson kontakt) {
        return new Kurs(id, tittel, beskrivelse, dato, "18:00–21:00", "1 kveld",
                "Demolokale i " + lokallag, lokallag, tema, "Voksne", "Nybegynner",
                "Avtal utstyr med kurskontakt.", new BigDecimal(medlemspris),
                new BigDecimal(ordinaerpris), 10, ledigePlasser, kontakt);
    }
}
