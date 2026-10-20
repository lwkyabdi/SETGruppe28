START TRANSACTION;
INSERT INTO kontaktperson (id, navn, epost, telefon, bilde) VALUES
(1, 'Kurskontakt Fredrikstad (demo)', 'fredrikstad@example.test', '00000000', '/bilder/kontakt-placeholder.svg'),
(2, 'Kurskontakt Sarpsborg (demo)', 'sarpsborg@example.test', '00000000', '/bilder/kontakt-placeholder.svg'),
(3, 'Kurskontakt Moss (demo)', 'moss@example.test', '00000000', '/bilder/kontakt-placeholder.svg'),
(4, 'Kurskontakt Halden (demo)', 'halden@example.test', '00000000', '/bilder/kontakt-placeholder.svg');

INSERT INTO kurs (id,tittel,beskrivelse,dato,tidspunkt,varighet,sted,lokallag,tema,malgruppe,niva,utstyr,medlemspris,ordinaerpris,kapasitet,ledige_plasser,status,kontakt_id) VALUES
(1,'Bunadssøm for nybegynnere','Lær grunnleggende håndsøm og arbeid med bunadsstoff. Dette er et demonstrasjonskurs.',DATE_ADD(CURRENT_DATE,INTERVAL 14 DAY),'18:00–21:00','8 kvelder','Demolokale i Fredrikstad','Fredrikstad','Bunad','Voksne','Nybegynner','Stoff, tråd og symaskin hvis du har.',1500,2000,8,3,'PUBLISERT',1),
(2,'Strikk din egen genser','Lær å lese oppskrift og strikke en enkel genser.',DATE_ADD(CURRENT_DATE,INTERVAL 21 DAY),'10:00–14:00','6 samlinger','Demolokale i Sarpsborg','Sarpsborg','Strikking','Voksne','Litt øvet','Garn og strikkepinner.',1200,1600,10,0,'PUBLISERT',2),
(3,'Vevkurs – grunnleggende båndveving','En innføring i enkel båndveving.',DATE_ADD(CURRENT_DATE,INTERVAL 28 DAY),'10:00–15:00','2 dager','Demolokale i Moss','Moss','Veving','Voksne','Nybegynner','Ta med notatbok. Demoen forutsetter tilgjengelig vev.',900,1200,10,6,'PUBLISERT',3),
(4,'Broderi og korssting','Lær enkle sting og lag et lite broderi.',DATE_ADD(CURRENT_DATE,INTERVAL 35 DAY),'18:00–20:30','4 kvelder','Demolokale i Halden','Halden','Brodering','Voksne','Nybegynner','Nål, tråd og broderistoff.',800,1100,12,8,'PUBLISERT',4),
(5,'Trearbeid – enkel treskjæring','Prøv grunnleggende treskjæring under veiledning.',DATE_ADD(CURRENT_DATE,INTERVAL 42 DAY),'09:00–15:00','1 dag','Demolokale i Fredrikstad','Fredrikstad','Trearbeid','Voksne','Nybegynner','Avklar verktøy med kurskontakt.',1100,1500,8,2,'PUBLISERT',1),
(6,'Søm og enkel konfeksjon','En innføring i mønster, tilskjæring og maskinsøm.',DATE_ADD(CURRENT_DATE,INTERVAL 49 DAY),'10:00–14:00','5 samlinger','Demolokale i Sarpsborg','Sarpsborg','Søm','Voksne','Nybegynner','Symaskin, stoff og tråd.',1100,1500,10,5,'PUBLISERT',2),
(7,'Tidligere strikkekurs (demo)','Brukes til å kontrollere at forsiden bare viser kommende kurs.',DATE_SUB(CURRENT_DATE,INTERVAL 7 DAY),'10:00–12:00','1 dag','Demolokale i Moss','Moss','Strikking','Voksne','Nybegynner','Strikkepinner.',500,700,8,0,'PUBLISERT',3),
(8,'Upublisert kurs (demo)','Denne kladden skal aldri vises i det offentlige API-et.',DATE_ADD(CURRENT_DATE,INTERVAL 60 DAY),'18:00–20:00','1 kveld','Demolokale','Halden','Søm','Voksne','Nybegynner','Ingen.',500,700,8,8,'KLADD',4);
COMMIT;
