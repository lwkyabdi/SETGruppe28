SELECT DATABASE() AS aktiv_database;

CREATE TABLE IF NOT EXISTS lokallag (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(80) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    navn VARCHAR(80) NOT NULL,
    beskrivelse TEXT NULL,
    adresse VARCHAR(255) NULL,
    epost VARCHAR(150) NULL,
    telefon VARCHAR(30) NULL,
    bildesti VARCHAR(255) NULL,
    aktiv BOOLEAN NOT NULL DEFAULT TRUE CHECK (aktiv IN (0, 1)),
    UNIQUE KEY uq_lokallag_slug (slug),
    UNIQUE KEY uq_lokallag_navn (navn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT INTO lokallag (slug, navn) VALUES
    ('fredrikstad', 'Fredrikstad'),
    ('sarpsborg', 'Sarpsborg'),
    ('moss', 'Moss'),
    ('halden', 'Halden'),
    ('indre-ostfold', 'Indre Østfold')
ON DUPLICATE KEY UPDATE id = lokallag.id;

CREATE TABLE IF NOT EXISTS administrator (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    navn VARCHAR(120) NOT NULL,
    epost VARCHAR(150) NOT NULL,
    passord_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    rolle VARCHAR(20) NOT NULL,
    lokallag_id BIGINT NULL,
    aktiv BOOLEAN NOT NULL DEFAULT TRUE CHECK (aktiv IN (0, 1)),
    UNIQUE KEY uq_administrator_epost (epost),
    CONSTRAINT fk_administrator_lokallag FOREIGN KEY (lokallag_id) REFERENCES lokallag (id),
    CONSTRAINT ck_administrator_rolle CHECK (
        (rolle = 'ADMIN' AND lokallag_id IS NULL)
        OR (rolle = 'LOKALLAG' AND lokallag_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- NULL i lokallag_id brukes for fellesnyheter.
CREATE TABLE IF NOT EXISTS artikkel (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tittel VARCHAR(180) NOT NULL,
    ingress VARCHAR(500) NOT NULL,
    innhold TEXT NOT NULL,
    bildesti VARCHAR(255) NULL,
    bildealt VARCHAR(180) NOT NULL DEFAULT '',
    lokallag_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'KLADD',
    publisert_tid TIMESTAMP NULL DEFAULT NULL,
    opprettet TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    oppdatert TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_artikkel_lokallag FOREIGN KEY (lokallag_id) REFERENCES lokallag (id),
    CONSTRAINT ck_artikkel_status CHECK (
        status = 'KLADD' OR (status = 'PUBLISERT' AND publisert_tid IS NOT NULL)
    ),
    KEY idx_artikkel_publisert (status, publisert_tid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- ID-telleren brukes med radlås ved oppretting av kurs.
CREATE TABLE IF NOT EXISTS id_teller (
    navn VARCHAR(40) CHARACTER SET ascii COLLATE ascii_bin NOT NULL PRIMARY KEY,
    siste_id BIGINT NOT NULL CHECK (siste_id >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT INTO id_teller (navn, siste_id) VALUES ('kurs', 0), ('kontaktperson', 0)
ON DUPLICATE KEY UPDATE siste_id = id_teller.siste_id;

SET @gruppe28_endring = 'ALTER TABLE kontaktperson
    ADD COLUMN IF NOT EXISTS verv VARCHAR(120) NULL,
    ADD COLUMN IF NOT EXISTS sted VARCHAR(80) NULL,
    ADD COLUMN IF NOT EXISTS vis_pa_kontaktside BOOLEAN NOT NULL DEFAULT FALSE
        CHECK (vis_pa_kontaktside IN (0, 1))';
PREPARE gruppe28_endring FROM @gruppe28_endring;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

SET @gruppe28_endring = 'ALTER TABLE kurs
    ADD COLUMN IF NOT EXISTS bildesti VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS bildealt VARCHAR(180) NOT NULL DEFAULT ''''';
PREPARE gruppe28_endring FROM @gruppe28_endring;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

-- Påmeldingsflyten bruker pamelding.status. Denne køtabellen er separat.
CREATE TABLE IF NOT EXISTS venteliste (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    kurs_id BIGINT NOT NULL,
    navn VARCHAR(160) NOT NULL,
    epost VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VENTER'
        CHECK (status IN ('VENTER', 'VARSLET', 'AVBRUTT', 'PAMELDT')),
    opprettet TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    varslet_tid TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_venteliste_kurs FOREIGN KEY (kurs_id) REFERENCES kurs (id),
    UNIQUE KEY uq_venteliste_kurs_epost (kurs_id, epost),
    KEY idx_venteliste_ko (kurs_id, status, opprettet, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

SET @gruppe28_endring = 'ALTER TABLE venteliste
    ADD COLUMN IF NOT EXISTS svarfrist TIMESTAMP NULL DEFAULT NULL';
PREPARE gruppe28_endring FROM @gruppe28_endring;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

CREATE TABLE IF NOT EXISTS kontaktmelding (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    navn VARCHAR(120) NOT NULL,
    epost VARCHAR(150) NOT NULL,
    melding TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NY' CHECK (status IN ('NY', 'BEHANDLET')),
    opprettet TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_kontaktmelding_status (status, opprettet, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS medlemsinteresse (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lokallag_id BIGINT NOT NULL,
    navn VARCHAR(120) NOT NULL,
    epost VARCHAR(150) NOT NULL,
    telefon VARCHAR(30) NOT NULL,
    samtykke BOOLEAN NOT NULL CHECK (samtykke = TRUE),
    status VARCHAR(20) NOT NULL DEFAULT 'NY' CHECK (status IN ('NY', 'BEHANDLET')),
    opprettet TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medlemsinteresse_lokallag FOREIGN KEY (lokallag_id) REFERENCES lokallag (id),
    KEY idx_medlemsinteresse_lag_status (lokallag_id, status, opprettet, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE IF NOT EXISTS aktivitet (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lokallag_id BIGINT NOT NULL,
    tittel VARCHAR(180) NOT NULL,
    beskrivelse TEXT NOT NULL,
    dato DATE NOT NULL,
    tidspunkt VARCHAR(60) NOT NULL,
    sted VARCHAR(180) NOT NULL,
    bildesti VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'KLADD' CHECK (status IN ('KLADD', 'PUBLISERT')),
    CONSTRAINT fk_aktivitet_lokallag FOREIGN KEY (lokallag_id) REFERENCES lokallag (id),
    KEY idx_aktivitet_status_dato (status, dato, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

SET @gruppe28_endring = 'ALTER TABLE aktivitet
    ADD COLUMN IF NOT EXISTS tema VARCHAR(60) NULL';
PREPARE gruppe28_endring FROM @gruppe28_endring;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

SHOW TABLES;
SELECT (SELECT COUNT(*) FROM kontaktperson) AS kontakter,
       (SELECT COUNT(*) FROM kurs) AS kurs,
       (SELECT COUNT(*) FROM pamelding) AS digitale_pameldinger,
       (SELECT COUNT(*) FROM lokallag) AS lokallag;
