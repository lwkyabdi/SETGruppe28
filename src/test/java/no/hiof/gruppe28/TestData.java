package no.hiof.gruppe28;

import no.hiof.gruppe28.model.Kontaktperson;
import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.repository.KursRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class TestData {
    private TestData() {}
    public static Kurs kurs(long id, LocalDate dato) {
        return new Kurs(id, "Kurs " + id, "Beskrivelse", dato,
                "18:00–20:00", "1 kveld", "Teststed", "Moss", "Veving",
                "Voksne", "Nybegynner", "Notatbok",
                new BigDecimal("100.00"), new BigDecimal("150.00"), 8, 3,
                new Kontaktperson(1, "Testkontakt", "kontakt@example.test", "00000000", "/bilder/kontakt-placeholder.svg"));
    }
    public static KursRepository repository(List<Kurs> data) {
        return new KursRepository() {
            public List<Kurs> findPubliserte() { return data; }
            public Optional<Kurs> findPublisert(long id) {
                return data.stream().filter(k -> k.id() == id).findFirst();
            }
        };
    }
}
