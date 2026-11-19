DELIMITER //
BEGIN NOT ATOMIC
    DECLARE tabeller TEXT;
    DECLARE skrivelaser TEXT;
    DECLARE radkontroller TEXT;
    DECLARE har_las BOOLEAN DEFAULT FALSE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        IF har_las THEN
            EXECUTE IMMEDIATE 'UNLOCK TABLES';
        END IF;
        RESIGNAL;
    END;

    IF DATABASE() IS NULL OR BINARY DATABASE() NOT IN ('se26_G28', 'husflid_test') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Velg se26_G28 eller husflid_test.';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pamelding' AND COLUMN_NAME = 'aktiv_epost'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Kontroller at migrering 08 er fullført.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE <> 'BASE TABLE'
          AND BINARY TABLE_NAME IN ('aktivitet', 'kontaktmelding', 'medlemsinteresse')
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Et av navnene brukes av et annet databaseobjekt. Kontroller skjemaet.';
    END IF;

    SELECT GROUP_CONCAT(TABLE_NAME ORDER BY TABLE_NAME SEPARATOR ', '),
           GROUP_CONCAT(CONCAT(TABLE_NAME, ' WRITE') ORDER BY TABLE_NAME SEPARATOR ', '),
           GROUP_CONCAT(CONCAT('EXISTS (SELECT 1 FROM ', TABLE_NAME, ')')
                        ORDER BY TABLE_NAME SEPARATOR ' OR ')
    INTO tabeller, skrivelaser, radkontroller
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
      AND BINARY TABLE_NAME IN ('aktivitet', 'kontaktmelding', 'medlemsinteresse');

    IF tabeller IS NOT NULL THEN
        EXECUTE IMMEDIATE CONCAT('LOCK TABLES ', skrivelaser);
        SET har_las = TRUE;

        EXECUTE IMMEDIATE CONCAT('SELECT ', radkontroller, ' INTO @g28_09_har_rader');
        IF @g28_09_har_rader THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Minst én tabell inneholder data. Ingen tabeller er fjernet.';
        END IF;
        IF EXISTS (
            SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
            WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
              AND BINARY REFERENCED_TABLE_NAME IN ('aktivitet', 'kontaktmelding', 'medlemsinteresse')
        ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'En fremmednøkkel peker til en av tabellene. Avklar avhengigheten først.';
        END IF;

        EXECUTE IMMEDIATE CONCAT('DROP TABLE ', tabeller);
        EXECUTE IMMEDIATE 'UNLOCK TABLES';
        SET har_las = FALSE;
    END IF;
END//
DELIMITER ;

SELECT DATABASE() AS valgt_database,
       (SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE') AS antall_tabeller,
       (SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()) AS antall_fremmednokler;
