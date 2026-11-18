ALTER TABLE pamelding
    ADD COLUMN IF NOT EXISTS ko_nummer BIGINT NULL,
    ADD COLUMN IF NOT EXISTS avmeldt_tid TIMESTAMP NULL DEFAULT NULL;

SET @g28_safe_updates = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET @g28_siste_ko = (SELECT COALESCE(MAX(ko_nummer), 0) FROM pamelding);

UPDATE pamelding p
JOIN (
    SELECT referanse, ROW_NUMBER() OVER (ORDER BY opprettet, referanse) AS nummer
    FROM pamelding WHERE ko_nummer IS NULL
) q ON q.referanse = p.referanse
SET p.ko_nummer = @g28_siste_ko + q.nummer
WHERE p.ko_nummer IS NULL;

SET SQL_SAFE_UPDATES = @g28_safe_updates;

SET @g28_ko_klar = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pamelding'
      AND COLUMN_NAME = 'ko_nummer' AND EXTRA = 'auto_increment'
);
SET @g28_sql = IF(@g28_ko_klar = 1,
    'SELECT ''Kønøkkelen finnes allerede'' AS melding',
    'ALTER TABLE pamelding ADD UNIQUE KEY uq_pamelding_ko (ko_nummer),
       MODIFY COLUMN ko_nummer BIGINT NOT NULL AUTO_INCREMENT'
);
PREPARE g28_endring FROM @g28_sql;
EXECUTE g28_endring;
DEALLOCATE PREPARE g28_endring;

SET @g28_aktiv_klar = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pamelding'
      AND INDEX_NAME = 'uq_pamelding_aktiv_epost' AND NON_UNIQUE = 0
);
SET @g28_sql = IF(@g28_aktiv_klar > 0,
    'SELECT ''Aktiv unikhet finnes allerede'' AS melding',
    'ALTER TABLE pamelding
       ADD COLUMN aktiv_epost VARCHAR(150) GENERATED ALWAYS AS
           (CASE WHEN status IN (''PAMELDT'', ''VENTELISTE'') THEN epost ELSE NULL END) PERSISTENT,
       ADD UNIQUE KEY uq_pamelding_aktiv_epost (kurs_id, aktiv_epost),
       DROP INDEX uq_pamelding_epost,
       DROP CONSTRAINT ck_pamelding_status,
       ADD CONSTRAINT ck_pamelding_status
           CHECK (status IN (''PAMELDT'', ''VENTELISTE'', ''AVMELDT'')),
       ADD CONSTRAINT ck_pamelding_avmeldt_tid CHECK
           ((status = ''AVMELDT'' AND avmeldt_tid IS NOT NULL)
             OR (status IN (''PAMELDT'', ''VENTELISTE'') AND avmeldt_tid IS NULL))'
);
PREPARE g28_endring FROM @g28_sql;
EXECUTE g28_endring;
DEALLOCATE PREPARE g28_endring;

CREATE INDEX IF NOT EXISTS idx_pamelding_kurs_status_ko
    ON pamelding (kurs_id, status, ko_nummer);

SELECT status, COUNT(*) AS antall FROM pamelding GROUP BY status;
SELECT COUNT(*) AS ugyldige_konokler FROM pamelding WHERE ko_nummer IS NULL OR ko_nummer < 1;
