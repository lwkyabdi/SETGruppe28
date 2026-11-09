package no.hiof.gruppe28;

import no.hiof.gruppe28.config.EpostConfig;
import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.epost.EpostTjeneste;
import no.hiof.gruppe28.epost.SmtpEpostTjeneste;
import no.hiof.gruppe28.model.PameldingStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

// Manuell test: sender én ekte e-post til gruppens egen adresse.
// Krever epostConfig.properties. Kjøres med den grønne pilen, ikke av Maven.
public class SendTestEpost {

    public static void main(String[] args) throws IOException {
        EpostConfig config = EpostConfig.load();
        EpostTjeneste epost = new SmtpEpostTjeneste(config);

        PameldingKvittering kvittering = PameldingKvittering.fra(
            TestData.kurs(1, LocalDate.of(2026, 12, 1)),
            "TEST-123",
            new BigDecimal("150.00"),
            false,
            PameldingStatus.PAMELDT
        );

        epost.sendBekreftelse(config.username(), kvittering);
        System.out.println("E-post sendt til " + config.username());
    }
}
