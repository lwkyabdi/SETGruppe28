package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.model.Nyhet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DemoNyhetRepository implements NyhetRepository {

    private final List<Nyhet> nyheter = List.of(
        new Nyhet(1,
            "Nytt bunadskurs i Fredrikstad – meld deg på nå!",
            LocalDate.of(2024, 9, 2),
            "Fredrikstad Husflidslag",
            "Vi åpner nå påmelding til høstens bunadskurs i Fredrikstad. Det er kun 8 plasser, og de fylles raskt.",
            List.of(
                "Vi er glade for å melde at Fredrikstad Husflidslag setter opp nytt bunadskurs til høsten. Kurset starter 20. september og går over 8 tirsdagskvelder.",
                "Kursleder er den erfarne Marit Andersen, som har over 20 år med bunadssøm bak seg. Hun tar imot alle nivåer og har stor tålmodighet med nybegynnere.",
                "Dette er en ypperlig mulighet til å lære en gammel håndverkstradisjon i et hyggelig miljø. Mange av deltakerne fra tidligere kurs fremhever de sosiale sidene som noe av det beste med å delta.",
                "Meld deg på så snart som mulig – det er allerede 5 av 8 plasser tatt."),
            "nyhet-1.jpg"),
        new Nyhet(2,
            "Ledige plasser på vevkurs i Moss",
            LocalDate.of(2024, 9, 5),
            "Moss Husflidslag",
            "Det er fortsatt ledige plasser på høstens vevkurs i Moss. Et flott kurs for deg som vil lære tradisjonell båndbinding.",
            List.of(
                "Moss Husflidslag arrangerer grunnkurs i båndveving 5. og 6. oktober. Kurset passer for nybegynnere, og alt utstyr finnes i vevstudioet vårt.",
                "Du lærer å sette opp en båndvev, velge garn og veve enkle mønstre. Etter to dager har du et ferdig bånd med deg hjem.",
                "Det er 6 ledige plasser igjen. Meld deg på via kurssiden."),
            "nyhet-2.jpg"),
        new Nyhet(3,
            "Høstprogrammet er klart – se alle kurs og aktiviteter",
            LocalDate.of(2024, 8, 28),
            "Østfold Husflidslag",
            "Vi har satt opp et rikt høstprogram med kurs innen strikking, veving, søm, broderi, bunad og trearbeid.",
            List.of(
                "Høstens program er nå publisert, og lokallagene i hele Østfold har satt opp kurs for både nybegynnere og erfarne håndverkere.",
                "Nytt i år er flere helgekurs og et eget kurs i tradisjonell treskjæring i Fredrikstad.",
                "Du kan delta på kurs i alle lokallag, uansett hvor du bor. Se hele oversikten under Kurs og aktiviteter."),
            "nyhet-3.jpg"),
        new Nyhet(4,
            "Velkommen til medlemskveld i september",
            LocalDate.of(2024, 8, 20),
            "Fredrikstad Husflidslag",
            "Alle medlemmer inviteres til en hyggelig sammenkomst hos Fredrikstad Husflidslag 14. september.",
            List.of(
                "Vi samles i lokalene våre i Nygårdsgata 14 lørdag 14. september klokken 18.",
                "Det blir kaffe og kake, en kort gjennomgang av høstens kurs og god tid til å vise frem det du har laget.",
                "Ta gjerne med en venn som er nysgjerrig på husflid. Alle er velkomne."),
            null)
    );

    @Override
    public List<Nyhet> findAlle() {
        return nyheter;
    }

    @Override
    public Optional<Nyhet> findById(long id) {
        return nyheter.stream()
            .filter(n -> n.id() == id)
            .findFirst();
    }
}
