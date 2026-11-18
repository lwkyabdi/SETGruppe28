-- Utvider kursstatus til KLADD, PUBLISERT og AVLYST.

SET @g28_statuskolonne = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
      AND COLUMN_NAME = 'status' AND DATA_TYPE = 'varchar'
      AND CHARACTER_MAXIMUM_LENGTH >= 9 AND IS_NULLABLE = 'NO'
      AND COLUMN_DEFAULT IS NULL AND EXTRA = '' AND COLUMN_COMMENT = ''
);

SET @g28_ukjente_regler = (
    SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
      AND CHECK_CLAUSE REGEXP '(^|[^[:alnum:]_])status([^[:alnum:]_]|$)'
      AND REGEXP_REPLACE(UPPER(CHECK_CLAUSE), '[[:space:]`()]+', '') NOT IN ('STATUSIN''PUBLISERT'',''KLADD''', 'STATUSIN''KLADD'',''PUBLISERT''', 'STATUSIN''PUBLISERT'',''KLADD'',''AVLYST''', 'STATUSIN''PUBLISERT'',''AVLYST'',''KLADD''', 'STATUSIN''KLADD'',''PUBLISERT'',''AVLYST''', 'STATUSIN''KLADD'',''AVLYST'',''PUBLISERT''', 'STATUSIN''AVLYST'',''PUBLISERT'',''KLADD''', 'STATUSIN''AVLYST'',''KLADD'',''PUBLISERT''')
);

SET @g28_navnekollisjon = (
    SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
      AND CONSTRAINT_NAME = 'ck_kurs_status_avlyst'
      AND REGEXP_REPLACE(UPPER(CHECK_CLAUSE), '[[:space:]`()]+', '') NOT IN ('STATUSIN''PUBLISERT'',''KLADD''', 'STATUSIN''KLADD'',''PUBLISERT''', 'STATUSIN''PUBLISERT'',''KLADD'',''AVLYST''', 'STATUSIN''PUBLISERT'',''AVLYST'',''KLADD''', 'STATUSIN''KLADD'',''PUBLISERT'',''AVLYST''', 'STATUSIN''KLADD'',''AVLYST'',''PUBLISERT''', 'STATUSIN''AVLYST'',''PUBLISERT'',''KLADD''', 'STATUSIN''AVLYST'',''KLADD'',''PUBLISERT''')
);

SET @g28_fjern = (
    SELECT GROUP_CONCAT(
        CONCAT('DROP CONSTRAINT `', REPLACE(CONSTRAINT_NAME, '`', '``'), '`')
        ORDER BY CONSTRAINT_NAME SEPARATOR ', '
    ) FROM information_schema.CHECK_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
      AND LEVEL = 'Table'
      AND REGEXP_REPLACE(UPPER(CHECK_CLAUSE), '[[:space:]`()]+', '') IN ('STATUSIN''PUBLISERT'',''KLADD''', 'STATUSIN''KLADD'',''PUBLISERT''')
);

SET @g28_kolonneregel = (
    SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
      AND LEVEL = 'Column'
      AND REGEXP_REPLACE(UPPER(CHECK_CLAUSE), '[[:space:]`()]+', '') IN ('STATUSIN''PUBLISERT'',''KLADD''', 'STATUSIN''KLADD'',''PUBLISERT''')
);

SET @g28_endre_kolonne = IF(@g28_kolonneregel = 0, '', (
    SELECT CONCAT('MODIFY COLUMN `status` ', COLUMN_TYPE,
        ' CHARACTER SET ', CHARACTER_SET_NAME,
        ' COLLATE ', COLLATION_NAME, ' NOT NULL')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs' AND COLUMN_NAME = 'status'
));

SET @g28_ny_regel_finnes = (
    SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
      AND REGEXP_REPLACE(UPPER(CHECK_CLAUSE), '[[:space:]`()]+', '') IN (
        'STATUSIN''PUBLISERT'',''KLADD'',''AVLYST''',
        'STATUSIN''PUBLISERT'',''AVLYST'',''KLADD''',
        'STATUSIN''KLADD'',''PUBLISERT'',''AVLYST''',
        'STATUSIN''KLADD'',''AVLYST'',''PUBLISERT''',
        'STATUSIN''AVLYST'',''PUBLISERT'',''KLADD''',
        'STATUSIN''AVLYST'',''KLADD'',''PUBLISERT'''
      )
);

SET @g28_legg_til = IF(@g28_ny_regel_finnes > 0, '',
    'ADD CONSTRAINT `ck_kurs_status_avlyst` CHECK (`status` IN (''KLADD'', ''PUBLISERT'', ''AVLYST''))'
);
SET @g28_endringer = CONCAT_WS(', ', NULLIF(@g28_endre_kolonne, ''),
    NULLIF(@g28_fjern, ''), NULLIF(@g28_legg_til, ''));

SET @g28_sql = IF(
    DATABASE() IN ('se26_G28', 'husflid_test')
      AND @g28_statuskolonne = 1 AND @g28_ukjente_regler = 0 AND @g28_navnekollisjon = 0,
    IF(@g28_endringer = '',
        'SELECT ''AVLYST er allerede tillatt'' AS melding',
        CONCAT('ALTER TABLE `kurs` ', @g28_endringer)
    ),
    'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''Velg riktig database og kontroller kurs.status i 00-kontroll-database.sql.'''
);
PREPARE g28_endring FROM @g28_sql;
EXECUTE g28_endring;
DEALLOCATE PREPARE g28_endring;

SELECT CONSTRAINT_NAME, CHECK_CLAUSE
FROM information_schema.CHECK_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'kurs'
  AND CHECK_CLAUSE REGEXP '(^|[^[:alnum:]_])status([^[:alnum:]_]|$)';

SELECT status, COUNT(*) AS antall FROM kurs GROUP BY status;
