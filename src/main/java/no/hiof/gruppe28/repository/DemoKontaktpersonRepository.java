package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Kontaktperson;

import java.util.List;

public class DemoKontaktpersonRepository implements KontaktpersonRepository {

    private final List<Kontaktperson> personer = List.of(
        new Kontaktperson(1, "Silje Ole", "silje@fredrikstad-husflid.no", "900 12 345",
            "hflogo.svg", "Sekretær og nettansvarlig", "Fredrikstad"),
        new Kontaktperson(2, "Kari Hansen", "kari@fredrikstad-husflid.no", "911 23 456",
            "hflogo.svg", "Kursansvarlig", "Fredrikstad"),
        new Kontaktperson(3, "Bjørg Nilsen", "bjorg@ostfold-husflid.no", "922 34 567",
            "hflogo.svg", "Leder", "Østfold"),
        new Kontaktperson(4, "Per Kristiansen", "per@ostfold-husflid.no", "933 45 678",
            "hflogo.svg", "Kasserer", "Østfold")
    );

    @Override
    public List<Kontaktperson> finnForKontaktside() {
        return personer;
    }
}
