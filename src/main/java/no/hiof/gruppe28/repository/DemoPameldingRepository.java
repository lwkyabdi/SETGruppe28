package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.model.Kurs;
import no.hiof.gruppe28.model.PameldingData;
import no.hiof.gruppe28.model.PameldingStatus;
import no.hiof.gruppe28.model.Registrering;
import no.hiof.gruppe28.service.PameldingException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DemoPameldingRepository implements PameldingRepository {

    private record Lagret(String foresporselHash, long kursId, String epost, PameldingKvittering kvittering) { }

    private final KursRepository kursRepository;
    private final Map<String, Lagret> pameldinger = new HashMap<>();
    private final Map<Long, Integer> brukteplasser = new HashMap<>();

    public DemoPameldingRepository(KursRepository kursRepository) {
        this.kursRepository = kursRepository;
    }

    @Override
    public synchronized Registrering registrer(long kursId, PameldingData data, LocalDate idag) {
        Kurs kurs = kursRepository.findPublisert(kursId)
            .orElseThrow(() -> new PameldingException(404, "Kurset finnes ikke."));

        // Samme forespørsel skal ikke registreres flere ganger.
        Lagret tidligere = pameldinger.get(data.referanse());
        if (tidligere != null) {
            if (!tidligere.foresporselHash().equals(data.foresporselHash())) {
                throw new PameldingException(409,
                    "Påmeldingsnøkkelen er allerede brukt med andre opplysninger.");
            }
            return new Registrering(tidligere.kvittering(), false);
        }

        PameldingRepository.kontrollerKurs(kurs, idag);

        // Én påmelding per e-postadresse på samme kurs.
        boolean alleredePameldt = pameldinger.values().stream()
            .anyMatch(p -> p.kursId() == kursId && p.epost().equals(data.epost()));
        if (alleredePameldt) {
            throw new PameldingException(409, "Denne e-postadressen er allerede påmeldt kurset.");
        }

        // Ledig plass gir påmeldt, fullt kurs gir venteliste.
        int ledige = kurs.ledigePlasser() - brukteplasser.getOrDefault(kursId, 0);
        PameldingStatus status = ledige > 0 ? PameldingStatus.PAMELDT : PameldingStatus.VENTELISTE;
        if (status == PameldingStatus.PAMELDT) {
            brukteplasser.merge(kursId, 1, Integer::sum);
            ledige--;
        }

        BigDecimal pris = data.medlem() ? kurs.medlemspris() : kurs.ordinaerpris();
        PameldingKvittering kvittering = PameldingKvittering.fra(
            kurs.medLedigePlasser(Math.max(ledige, 0)), data.referanse(), pris, true, status);

        pameldinger.put(data.referanse(),
            new Lagret(data.foresporselHash(), kursId, data.epost(), kvittering));
        return new Registrering(kvittering, true);
    }

    @Override
    public boolean erDemo() {
        return true;
    }
}
