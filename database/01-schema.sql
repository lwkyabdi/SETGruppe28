CREATE TABLE IF NOT EXISTS kontaktperson (
    id BIGINT PRIMARY KEY,
    navn VARCHAR(120) NOT NULL,
    epost VARCHAR(150) NOT NULL,
    telefon VARCHAR(30) NOT NULL,
    bilde VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS kurs (
    id BIGINT PRIMARY KEY,
    tittel VARCHAR(180) NOT NULL,
    beskrivelse TEXT NOT NULL,
    dato DATE,
    tidspunkt VARCHAR(60) NOT NULL,
    varighet VARCHAR(80) NOT NULL,
    sted VARCHAR(180) NOT NULL,
    lokallag VARCHAR(80) NOT NULL,
    tema VARCHAR(60) NOT NULL,
    malgruppe VARCHAR(100) NOT NULL,
    niva VARCHAR(60) NOT NULL,
    utstyr TEXT NOT NULL,
    medlemspris DECIMAL(10,2) NOT NULL CHECK (medlemspris >= 0),
    ordinaerpris DECIMAL(10,2) NOT NULL CHECK (ordinaerpris >= 0),
    kapasitet INT NOT NULL CHECK (kapasitet > 0),
    ledige_plasser INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PUBLISERT', 'KLADD')),
    kontakt_id BIGINT NOT NULL,
    CONSTRAINT fk_kurs_kontakt FOREIGN KEY (kontakt_id) REFERENCES kontaktperson(id),
    CONSTRAINT ck_ledige CHECK (ledige_plasser >= 0 AND ledige_plasser <= kapasitet),
    INDEX idx_kurs_status_dato (status, dato)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
