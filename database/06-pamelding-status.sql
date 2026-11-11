-- Ved oppretting av statusfeltet får eksisterende påmeldinger PAMELDT.
SET @gruppe28_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pamelding'
          AND COLUMN_NAME = 'status'
    ),
    'SELECT ''Statusfeltet finnes allerede'' AS melding',
    'ALTER TABLE pamelding
       ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''PAMELDT'''
);
PREPARE gruppe28_endring FROM @gruppe28_sql;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

SET @gruppe28_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pamelding'
          AND CONSTRAINT_NAME = 'ck_pamelding_status'
          AND CONSTRAINT_TYPE = 'CHECK'
    ),
    'SELECT ''Statuskontrollen finnes allerede'' AS melding',
    'ALTER TABLE pamelding
       ADD CONSTRAINT ck_pamelding_status
       CHECK (status IS NOT NULL AND status IN (''PAMELDT'', ''VENTELISTE''))'
);
PREPARE gruppe28_endring FROM @gruppe28_sql;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

SET @gruppe28_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'pamelding'
          AND INDEX_NAME = 'idx_pamelding_kurs_status_tid'
    ),
    'SELECT ''Indeksen finnes allerede'' AS melding',
    'CREATE INDEX idx_pamelding_kurs_status_tid
       ON pamelding (kurs_id, status, opprettet, referanse)'
);
PREPARE gruppe28_endring FROM @gruppe28_sql;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'pamelding'
  AND COLUMN_NAME = 'status';

SELECT status, COUNT(*) AS antall
FROM pamelding
GROUP BY status;

SELECT COUNT(*) AS ugyldige_statuser
FROM pamelding
WHERE status IS NULL OR status NOT IN ('PAMELDT', 'VENTELISTE');

SELECT id, tittel, kapasitet, ledige_plasser
FROM kurs
ORDER BY id;
