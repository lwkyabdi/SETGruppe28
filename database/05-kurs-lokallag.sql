SET @gruppe28_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'kurs'
          AND COLUMN_NAME = 'lokallag_id'
    ),
    'SELECT ''kurs.lokallag_id finnes allerede'' AS melding',
    'ALTER TABLE kurs ADD COLUMN lokallag_id BIGINT NULL'
);
PREPARE gruppe28_endring FROM @gruppe28_sql;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

SET @gruppe28_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'kurs'
          AND COLUMN_NAME = 'lokallag_id'
          AND REFERENCED_TABLE_SCHEMA = DATABASE()
          AND REFERENCED_TABLE_NAME = 'lokallag'
          AND REFERENCED_COLUMN_NAME = 'id'
    ),
    'SELECT ''Fremmednøkkelen til lokallag finnes allerede'' AS melding',
    'ALTER TABLE kurs ADD CONSTRAINT fk_kurs_lokallag
        FOREIGN KEY (lokallag_id) REFERENCES lokallag(id)'
);
PREPARE gruppe28_endring FROM @gruppe28_sql;
EXECUTE gruppe28_endring;
DEALLOCATE PREPARE gruppe28_endring;

-- Safe updates slås midlertidig av for UPDATE med JOIN.
SET @gruppe28_safe_updates = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

START TRANSACTION;

UPDATE kurs AS k
JOIN lokallag AS l ON l.navn = k.lokallag
SET k.lokallag_id = l.id
WHERE k.lokallag_id IS NULL;

COMMIT;

SET SQL_SAFE_UPDATES = @gruppe28_safe_updates;

SELECT k.id AS kurs_id, k.tittel, k.lokallag AS eksisterende_navn,
       k.lokallag_id, l.navn AS koblet_lokallag
FROM kurs AS k
LEFT JOIN lokallag AS l ON l.id = k.lokallag_id
ORDER BY k.id;

SELECT COUNT(*) AS kurs_uten_lokallag_id
FROM kurs
WHERE lokallag_id IS NULL;

SELECT k.id AS kurs_id, k.tittel, k.lokallag AS eksisterende_navn,
       k.lokallag_id, l.navn AS koblet_lokallag
FROM kurs AS k
JOIN lokallag AS l ON l.id = k.lokallag_id
WHERE k.lokallag <> l.navn
ORDER BY k.id;

SELECT CONSTRAINT_NAME, COLUMN_NAME,
       REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'kurs'
  AND COLUMN_NAME = 'lokallag_id'
  AND REFERENCED_TABLE_NAME = 'lokallag';
