package no.hiof.gruppe28.repository;

import no.hiof.gruppe28.config.DatabaseConfig;
import no.hiof.gruppe28.dto.PameldingKvittering;
import no.hiof.gruppe28.model.PameldingData;
import no.hiof.gruppe28.service.PameldingException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;

public class JdbcPameldingRepository implements PameldingRepository {

    private final DatabaseConfig database;

    public JdbcPameldingRepository(DatabaseConfig database) {
        this.database = database;
    }

    @Override
    public PameldingKvittering registrer(
            long kursId,
            PameldingData data,
            LocalDate idag
    ) {
        try (Connection c = database.connect()) {
            c.setAutoCommit(false);

            try {
                // Låser kursraden mens påmeldingen behandles.
                var kurs = JdbcKursRepository.findPublisert(c, kursId, true)
                        .orElseThrow(() -> new PameldingException(
                                404,
                                "Kurset finnes ikke."
                        ));

                // Samme forespørsel skal ikke registreres flere ganger.
                try (var s = c.prepareStatement(
                        "SELECT * FROM pamelding WHERE referanse = ?"
                )) {
                    s.setString(1, data.referanse());

                    try (var r = s.executeQuery()) {
                        if (r.next()) {
                            if (!data.foresporselHash().equals(
                                    r.getString("foresporsel_hash")
                            )) {
                                throw new PameldingException(
                                        409,
                                        "Påmeldingsnøkkelen er allerede brukt "
                                                + "med andre opplysninger."
                                );
                            }

                            var kvittering = new PameldingKvittering(
                                    data.referanse(),
                                    r.getLong("kurs_id"),
                                    r.getString("kurs_tittel"),
                                    r.getDate("kurs_dato").toString(),
                                    r.getString("kurs_tidspunkt"),
                                    r.getString("kurs_sted"),
                                    r.getString("kurs_lokallag"),
                                    r.getBigDecimal("pris"),
                                    kurs.ledigePlasser(),
                                    false
                            );

                            c.commit();
                            return kvittering;
                        }
                    }
                }

                PameldingRepository.kontrollerKurs(kurs, idag);

                // Én påmelding per e-postadresse på samme kurs.
                try (var s = c.prepareStatement(
                        """
                        SELECT referanse
                        FROM pamelding
                        WHERE kurs_id = ? AND epost = ?
                        """
                )) {
                    s.setLong(1, kursId);
                    s.setString(2, data.epost());

                    try (var r = s.executeQuery()) {
                        if (r.next()) {
                            throw new PameldingException(
                                    409,
                                    "Denne e-postadressen er allerede "
                                            + "påmeldt kurset."
                            );
                        }
                    }
                }

                var pris = data.medlem()
                        ? kurs.medlemspris()
                        : kurs.ordinaerpris();

                try (var s = c.prepareStatement(
                        """
                        INSERT INTO pamelding (
                            referanse,
                            foresporsel_hash,
                            kurs_id,
                            fornavn,
                            etternavn,
                            epost,
                            telefon,
                            fodselsar,
                            medlem,
                            medlemsnummer,
                            kommentar,
                            samtykke,
                            pris,
                            kurs_tittel,
                            kurs_dato,
                            kurs_tidspunkt,
                            kurs_sted,
                            kurs_lokallag
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                            TRUE, ?, ?, ?, ?, ?, ?
                        )
                        """
                )) {
                    s.setString(1, data.referanse());
                    s.setString(2, data.foresporselHash());
                    s.setLong(3, kursId);
                    s.setString(4, data.fornavn());
                    s.setString(5, data.etternavn());
                    s.setString(6, data.epost());
                    s.setString(7, data.telefon());
                    s.setInt(8, data.fodselsar());
                    s.setBoolean(9, data.medlem());
                    s.setString(10, data.medlemsnummer());
                    s.setString(11, data.kommentar());
                    s.setBigDecimal(12, pris);
                    s.setString(13, kurs.tittel());
                    s.setDate(14, Date.valueOf(kurs.dato()));
                    s.setString(15, kurs.tidspunkt());
                    s.setString(16, kurs.sted());
                    s.setString(17, kurs.lokallag());

                    s.executeUpdate();
                }

                try (var s = c.prepareStatement(
                        """
                        UPDATE kurs
                        SET ledige_plasser = ledige_plasser - 1
                        WHERE id = ? AND ledige_plasser > 0
                        """
                )) {
                    s.setLong(1, kursId);

                    if (s.executeUpdate() != 1) {
                        throw new PameldingException(
                                409,
                                "Kurset er fullt."
                        );
                    }
                }

                c.commit();

                return PameldingKvittering.fra(
                        kurs.medLedigePlasser(kurs.ledigePlasser() - 1),
                        data.referanse(),
                        pris,
                        false
                );

            } catch (SQLException | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }

                if (e instanceof SQLIntegrityConstraintViolationException sql
                        && sql.getErrorCode() == 1062) {
                    throw new PameldingException(
                            409,
                            "Påmeldingen er allerede registrert. "
                                    + "Kontroller opplysningene."
                    );
                }

                throw e;
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}