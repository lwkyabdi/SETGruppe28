CREATE TABLE IF NOT EXISTS pamelding (
    referanse CHAR(36) PRIMARY KEY,
    foresporsel_hash CHAR(64) NOT NULL,
    kurs_id BIGINT NOT NULL,

    fornavn VARCHAR(80) NOT NULL,
    etternavn VARCHAR(80) NOT NULL,
    epost VARCHAR(150) NOT NULL,
    telefon VARCHAR(20) NOT NULL,
    fodselsar INT NOT NULL CHECK (fodselsar >= 1900),
    medlem BOOLEAN NOT NULL,
    medlemsnummer VARCHAR(40) NOT NULL DEFAULT '',
    kommentar TEXT NOT NULL,
    samtykke BOOLEAN NOT NULL CHECK (samtykke = TRUE),

    pris DECIMAL(10,2) NOT NULL CHECK (pris >= 0),
    kurs_tittel VARCHAR(180) NOT NULL,
    kurs_dato DATE NOT NULL,
    kurs_tidspunkt VARCHAR(60) NOT NULL,
    kurs_sted VARCHAR(180) NOT NULL,
    kurs_lokallag VARCHAR(80) NOT NULL,

    opprettet TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pamelding_kurs
        FOREIGN KEY (kurs_id) REFERENCES kurs(id),

    CONSTRAINT uq_pamelding_epost
        UNIQUE (kurs_id, epost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
