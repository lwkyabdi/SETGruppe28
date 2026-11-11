package no.hiof.gruppe28.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class BildeService {

    public static final long MAKS_STORRELSE = 2 * 1024 * 1024; // 2 MB

    private final Path mappe;
    private final String adresse;


    public BildeService(Path mappe, String adresse) {
        this.mappe = mappe;
        this.adresse = adresse;
        try {
            Files.createDirectories(mappe);
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lage mappen for bilder.", e);
        }
    }

    public Path mappe() {
        return mappe;
    }

    public String adresse() {
        return adresse;
    }


    public String lagre(long storrelse, InputStream innhold) {
        if (storrelse <= 0) {
            throw new ApiException(400, "Velg et bilde.");
        }
        if (storrelse > MAKS_STORRELSE) {
            throw new ApiException(400, "Bildet kan være maks 2 MB.");
        }

        byte[] bytes;
        try {
            bytes = innhold.readNBytes((int) MAKS_STORRELSE + 1);
        } catch (IOException e) {
            throw new ApiException(400, "Kunne ikke lese bildet.");
        }
        if (bytes.length > MAKS_STORRELSE) {
            throw new ApiException(400, "Bildet kan være maks 2 MB.");
        }

        String endelse = endelse(bytes);
        if (endelse == null) {
            throw new ApiException(400, "Bare JPG, PNG og WebP er tillatt.");
        }

        String filnavn = UUID.randomUUID() + endelse;
        try {
            Files.write(mappe.resolve(filnavn), bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lagre bildet.", e);
        }
        return adresse + "/" + filnavn;
    }


    static String endelse(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
            && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return ".png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
            && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return ".webp";
        }
        return null;
    }
}
