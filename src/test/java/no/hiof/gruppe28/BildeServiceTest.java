package no.hiof.gruppe28;

import no.hiof.gruppe28.service.ApiException;
import no.hiof.gruppe28.service.BildeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BildeServiceTest {


    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0, 0};

    @TempDir
    Path mappe;

    private BildeService service;

    @BeforeEach
    void start() {
        service = new BildeService(mappe, "/opplastinger");
    }

    private String lagre(byte[] innhold) {
        return service.lagre(innhold.length, new ByteArrayInputStream(innhold));
    }

    private Path filFra(String adresse) {
        return mappe.resolve(adresse.substring("/opplastinger/".length()));
    }

    @Test
    void jpgLagresMedRiktigEndelse() {
        String adresse = lagre(JPG);

        assertTrue(adresse.startsWith("/opplastinger/"));
        assertTrue(adresse.endsWith(".jpg"));
        assertTrue(Files.exists(filFra(adresse)));
    }

    @Test
    void pngLagresMedRiktigEndelse() {
        assertTrue(lagre(PNG).endsWith(".png"));
    }

    @Test
    void webpLagresMedRiktigEndelse() {
        assertTrue(lagre(WEBP).endsWith(".webp"));
    }

    @Test
    void tekstfilAvvisesOgLagresIkke() throws Exception {
        ApiException feil = assertThrows(ApiException.class, () -> lagre("<script>alert(1)</script>".getBytes()));

        assertEquals(400, feil.status());
        try (var filer = Files.list(mappe)) {
            assertEquals(0, filer.count());
        }
    }

    @Test
    void forStortBildeAvvises() {
        byte[] stort = new byte[(int) BildeService.MAKS_STORRELSE + 1];
        System.arraycopy(JPG, 0, stort, 0, JPG.length);

        assertEquals(400, assertThrows(ApiException.class, () -> lagre(stort)).status());
    }

    @Test
    void tomFilAvvises() {
        assertEquals(400, assertThrows(ApiException.class, () -> lagre(new byte[0])).status());
    }

    @Test
    void toOpplastingerFarForskjelligeNavn() {
        assertNotEquals(lagre(JPG), lagre(JPG));
    }
}
