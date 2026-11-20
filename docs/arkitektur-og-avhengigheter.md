# Arkitektur og avhengigheter

Javalin leverer nettsidene og JSON-API-et fra samme server. Nettleseren bruker HTML, CSS og vanlig JavaScript. Det offentlige nettstedet bruker `script.js`, og administrasjonssidene bruker `minside/admin.js`. Begge henter data med `fetch`. Nettstedet har ingen egen frontend-byggeprosess. Stilarkene henter skrifttypene Lora og Source Sans 3 fra Google Fonts. Forsiden og «Om oss» viser den lokale bildefilen `bilder/hero.jpg`.

## Fra nettleser til database

| Del | Ansvar | Eksempel |
| --- | --- | --- |
| `Application` | Registrerer ruter og gir tjenestene riktig repository ved oppstart | Vanlig oppstart bruker JDBC. Demomodus bruker eksempeldata i minnet. |
| `controller` og `dto` | Leser forespørsler og bestemmer hva API-et sender tilbake | En påmelding leses som `PameldingRequest`. Svaret inneholder en `PameldingKvittering`. |
| `service` | Håndterer reglene for blant annet påmelding, publisering og lagtilgang | `PameldingService` kontrollerer e-post, samtykke og andre skjemafelt før lagring. |
| `repository` og `model` | Henter og lagrer data. Modellene beskriver blant annet kurs, kontaktpersoner, nyheter og påmeldinger. | `JdbcKursRepository` henter kurs og kontaktperson i samme spørring. |
| `config` og `epost` | Leser lokale innstillinger og velger hvordan e-post sendes | En SMTP-konfigurasjon gir utsending. Ellers vises meldingen i terminalen. |

Servicene bruker repository-grensesnitt. Vanlig oppstart kobler opp ni JDBC-repositoryer. Fem brukes for kurs, påmeldinger, nyheter, lokallag og kontaktpersoner. Ett brukes for innlogging, og tre for administrasjon av kurs, nyheter og påmeldinger. Tjenestene kan derfor også testes med testdata. I demomodus registreres de offentlige rutene og påmeldinger lagres i minnet til serveren stoppes.

## MariaDB og påmeldinger

Databasen har åtte tabeller etter migrering 09.

| Tabell | Bruk |
| --- | --- |
| `kurs` | Dato, priser, kapasitet, ledige plasser og ansvarlig kontaktperson. Kurset kan ha en kobling til et lokallag. |
| `kontaktperson` | Kontaktopplysninger for kurs. `vis_pa_kontaktside` bestemmer hvem som vises på kontaktsiden. |
| `lokallag` | Opplysninger om lagene som kurs, administratorer og nyheter kan knyttes til. |
| `pamelding` | Deltaker, status, referanse og kursopplysningene som gjaldt da deltakeren meldte seg på. |
| `venteliste` | Separat tabell knyttet til kurs. Påmeldingsskjemaet bruker `pamelding.status` til sin venteliste. |
| `administrator` | Konto, passordhash og rolle. Lokallagskontoer har en `lokallag_id`. |
| `artikkel` | Nyhet med publiseringsstatus og eventuell tilknytning til lokallag. |
| `id_teller` | Navngitte tellere. Kursadministrasjonen bruker telleren når den tildeler en ny kurs-ID. |

Repositoryene åpner JDBC-forbindelser med `DatabaseConfig.connect()` og lukker dem med `try-with-resources`. Ved påmelding låser `JdbcPameldingRepository` kursraden og lagrer påmeldingen i samme transaksjon som oppdateringen av ledige plasser. Ved fullt kurs får deltakeren status `VENTELISTE` uten å bruke en plass. En administrator kan senere velge «Gi plass». Ved avmelding beholdes raden som `AVMELDT`.

`ko_nummer` er en løpenøkkel på tvers av kurs. Databasen tillater én aktiv registrering per kurs og e-postadresse. Java-oppslaget sjekker også avmeldte rader. Påmelding til samme kurs med samme adresse og ny referanse blir derfor avvist også etter avmelding. Ved avlysning får selve kurset status `AVLYST` og forsvinner fra den offentlige kurslisten. Deltakernes historikk beholdes.

## Bilder og e-post

Administratorer kan velge JPG-, PNG- og WebP-bilder på opptil 10 MB i nettleseren. Nettleseren begrenser bredden til 1200 piksler og sender en JPEG-versjon. Serverens opplastingsgrense er 2 MB. `BildeService` lagrer filen under `opplastinger/`, og databasen lagrer nettstien. Bilder som følger med nettsiden, ligger i `src/main/resources/public/bilder/`.

Påmeldingen lagres før tjenesten forsøker å sende bekreftelsen. En SMTP-feil opphever derfor ikke en registrering. Uten `epostConfig.properties` bruker applikasjonen konsollen. Demomodus bruker alltid konsollen.

## Avhengigheter

Java-koden kompileres for Java 17. Maven Wrapper bruker Maven 3.9.11. Tabellen viser de direkte bibliotekene i `pom.xml`.

| Bibliotek | Versjon | Omfang | Bruk |
| --- | --- | --- | --- |
| `io.javalin:javalin` | 7.2.3 | compile | HTTP-ruter og levering av statiske filer |
| `com.fasterxml.jackson.core:jackson-databind` | 2.20.1 | compile | Lese og skrive JSON |
| `org.slf4j:slf4j-simple` | 2.0.17 | compile | Logging for biblioteker som bruker SLF4J |
| `com.mysql:mysql-connector-j` | 9.7.0 | runtime | JDBC-driver til MariaDB |
| `org.eclipse.angus:angus-mail` | 2.0.3 | compile | E-postbekreftelser via SMTP |
| `org.mindrot:jbcrypt` | 0.4 | compile | Lage passordhasher og kontrollere passord ved innlogging |
| `org.junit.jupiter:junit-jupiter` | 5.13.4 | test | Enhets-, API- og JDBC-tester |

Bibliotekversjonene er låst i `pom.xml`. Ved oppgradering kan samme testsett kjøres med en separat `husflid_test`. [README](../README.md) beskriver oppsettet.
