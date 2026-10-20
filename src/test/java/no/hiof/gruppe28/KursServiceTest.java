package no.hiof.gruppe28;

import no.hiof.gruppe28.service.KursService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static no.hiof.gruppe28.TestData.kurs;
import static org.junit.jupiter.api.Assertions.*;

class KursServiceTest {
    private final LocalDate idag = LocalDate.of(2026, 10, 17);

    @Test void fortidFjernesOgFremtidSorteres() {
        var result = KursService.getKommendeKurs(
                List.of(kurs(1, idag.plusDays(10)), kurs(2, idag.minusDays(1)), kurs(3, idag.plusDays(2))), idag);
        assertEquals(List.of(3L, 1L), result.stream().map(k -> k.id()).toList());
    }
    @Test void dagensKursErMed() {
        assertEquals(1, KursService.getKommendeKurs(List.of(kurs(1, idag)), idag).size());
    }
    @Test void tomListeErTrygg() {
        assertTrue(KursService.getKommendeKurs(List.of(), idag).isEmpty());
    }
    @Test void nullListeErTrygg() {
        assertTrue(KursService.getKommendeKurs(null, idag).isEmpty());
    }
    @Test void kursUtenDatoOgNullElementHoppesOver() {
        var result = KursService.getKommendeKurs(Arrays.asList(null, kurs(1, null), kurs(2, idag)), idag);
        assertEquals(List.of(2L), result.stream().map(k -> k.id()).toList());
    }
    @Test void inputListenEndresIkke() {
        var data = List.of(kurs(1, idag.plusDays(2)), kurs(2, idag));
        KursService.getKommendeKurs(data, idag);
        assertEquals(1, data.get(0).id());
    }
}
