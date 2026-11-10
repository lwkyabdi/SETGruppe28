package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Lokallag;

import java.util.List;

public class DemoLokallagRepository implements LokallagRepository {

    private final List<Lokallag> lokallag = List.of(
        new Lokallag(1, "fredrikstad", "Fredrikstad Husflidslag",
            "Kurs og aktiviteter gjennom hele året i Fredrikstad.", null, null, null, null),
        new Lokallag(2, "halden", "Halden Husflidslag",
            "Husflid og håndverk i Halden.", null, null, null, null),
        new Lokallag(3, "indre-ostfold", "Indre Østfold Husflidslag",
            "Kurs for store og små i Indre Østfold.", null, null, null, null),
        new Lokallag(4, "moss", "Moss Husflidslag",
            "Vevstudio og kurs i Moss.", null, null, null, null),
        new Lokallag(5, "sarpsborg", "Sarpsborg Husflidslag",
            "Tradisjonshåndverk i Sarpsborg.", null, null, null, null)
    );

    @Override
    public List<Lokallag> finnAktive() {
        return lokallag;
    }
}
