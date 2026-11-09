package no.hiof.gruppe28.epost;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import no.hiof.gruppe28.config.EpostConfig;
import no.hiof.gruppe28.dto.PameldingKvittering;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class SmtpEpostTjeneste implements EpostTjeneste {

    private final Session session;
    private final String avsender;

    public SmtpEpostTjeneste(EpostConfig config) {
        Properties innstillinger = new Properties();
        innstillinger.put("mail.smtp.host", config.host());
        innstillinger.put("mail.smtp.port", String.valueOf(config.port()));
        innstillinger.put("mail.smtp.auth", "true");
        innstillinger.put("mail.smtp.starttls.enable", "true");
        innstillinger.put("mail.smtp.starttls.required", "true");
        innstillinger.put("mail.smtp.connectiontimeout", "10000");
        innstillinger.put("mail.smtp.timeout", "10000");

        this.session = Session.getInstance(innstillinger, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username(), config.password());
            }
        });
        this.avsender = config.username();
    }

    @Override
    public void sendBekreftelse(String mottaker, PameldingKvittering kvittering) {
        try {
            MimeMessage melding = new MimeMessage(session);
            melding.setFrom(new InternetAddress(avsender, "Fredrikstad Husflidslag", "UTF-8"));
            melding.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mottaker));
            melding.setSubject(BekreftelsesTekst.emne(kvittering), "UTF-8");
            melding.setText(BekreftelsesTekst.tekst(kvittering), "UTF-8");

            Transport.send(melding);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Kunne ikke sende bekreftelse på e-post.", e);
        }
    }
}
