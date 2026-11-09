package no.hiof.gruppe28;

import no.hiof.gruppe28.model.Nyhet;
import no.hiof.gruppe28.repository.DemoNyhetRepository;
import no.hiof.gruppe28.service.NyhetService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NyhetServiceTest {

    private final NyhetService service = new NyhetService(new DemoNyhetRepository());

    @Test
    void alle_gir_nyeste_nyhet_forst() {
        List<Long> ider = service.alle().stream()
            .map(Nyhet::id)
            .toList();

        assertEquals(List.of(2L, 1L, 3L, 4L), ider);
    }

    @Test
    void finn_gir_riktig_nyhet() {
        Nyhet nyhet = service.finn(2).orElseThrow();

        assertEquals("Ledige plasser på vevkurs i Moss", nyhet.tittel());
    }

    @Test
    void finn_gir_tomt_svar_naar_nyheten_ikke_finnes() {
        assertTrue(service.finn(99).isEmpty());
    }
}
