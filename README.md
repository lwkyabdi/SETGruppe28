# Østfold Husflidslag – Gruppe 28

Nettside for kurs, aktiviteter og informasjon fra Østfold Husflidslag.
Prosjektet utvikles i emnet Software Engineering og Testing ved Høgskolen i Østfold.

Målet er å gjøre det enklere å finne relevante kurs, lese om aktivitetene
i lokallagene og finne kontaktinformasjon.

## Teknologi

- Frontend: HTML, CSS, JavaScript og Vue.
- Backend: Java og Javalin.
- Database: MySQL på HiØs server.
- Bygg og avhengigheter: Maven Wrapper.
- Automatiserte tester: JUnit.
- Versjonskontroll og samarbeid: Git og GitHub.

Frontend og backend kjøres av samme server på port 8080.

## Funksjoner og status

Løsningen har foreløpig:

- Forside med kommende kurs og nyheter.
- Kursoversikt med søk og filtrering på tema og lokallag.
- Kursdetaljer med dato, sted, pris, utstyr og kontaktperson.
- Kontaktoversikt.
- Navigasjon og visninger tilpasset mobil.

Kurs hentes fra MySQL ved vanlig oppstart. Nyhetene ligger foreløpig
som statiske eksempeldata i JavaScript.

Innlogging, administrasjon og digital påmelding er ikke implementert.

## Prosjektstruktur

| Mappe eller fil | Innhold |
| --- | --- |
| `src/main/java/` | Backend, API og databasetilgang |
| `src/main/resources/public/` | HTML, CSS, JavaScript og bilder |
| `src/test/java/` | Automatiserte tester |
| `database/` | SQL-skjema og eksempeldata |
| `docs/` | Teknisk dokumentasjon og manuell testing |
| `pom.xml` | Avhengigheter og byggekonfigurasjon |

## Oppstart

Kommandoene nedenfor kjøres i PowerShell fra prosjektmappen.

### Forutsetninger

- Git.
- JDK 21.
- Tilgang til prosjektets MySQL-database ved vanlig oppstart.

Maven Wrapper laster ned Maven automatisk ved første kjøring.

### 1. Klon prosjektet

```powershell
git clone https://github.com/lwkyabdi/SETGruppe28.git
cd SETGruppe28
```

### 2. Klargjør databasen

Prosjektets utviklingsdatabase heter `se26_G28` og ligger på
`itstud.hiof.no`, port `3306`.

Hvis prosjektets tabeller allerede er opprettet, går du videre til
databasekonfigurasjonen.

Ved førstegangsoppsett velger du utviklingsdatabasen i MySQL Workbench
og kjører filene i denne rekkefølgen:

1. `database/01-schema.sql`
2. `database/02-seed.sql`

Den første filen oppretter tabellene. Den andre legger inn eksempeldata.
Eksempeldataene importeres én gang i en tom database, ikke for hvert gruppemedlem.

### 3. Konfigurer databasetilkoblingen

Kopier konfigurasjonsmalen:

```powershell
Copy-Item dbConfig.example.properties dbConfig.properties
```

Åpne `dbConfig.properties` og fyll inn:

```properties
db.url=jdbc:mysql://itstud.hiof.no:3306/se26_G28?connectionTimeZone=Europe/Oslo
db.username=gruppe28
db.password=FYLL_INN_DATABASEPASSORD
```

Erstatt passordfeltet med databasepassordet dere har fått fra HiØ.

`dbConfig.properties` er utelatt fra Git.
`dbConfig.example.properties` inneholder konfigurasjonsmalen som deles i repoet.

### 4. Start applikasjonen

```powershell
.\mvnw.cmd compile exec:java
```

Åpne [localhost:8080](http://localhost:8080/).

Stopp serveren med **Ctrl + C**.

### Valgfri oppstart uten database

Grensesnittet kan også prøves med kursdata i minnet:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.args=--demo"
```

Denne modusen krever ingen databasekonfigurasjon. Kursdatoene beregnes
ved oppstart, og dataene opprettes på nytt hver gang applikasjonen starter.

Vanlig oppstart bruker MySQL. Data i minnet brukes bare med `--demo`.

På macOS og Linux brukes `sh mvnw` i stedet for `.\mvnw.cmd`
i kommandoene ovenfor.

## Tester

Kjør de automatiserte testene:

```powershell
.\mvnw.cmd test
```

Service-, API- og demo-testene kan kjøres uten MySQL.

MySQL-testene aktiveres med miljøvariablene:

- `MYSQL_TEST_URL`
- `MYSQL_TEST_USER`
- `MYSQL_TEST_PASSWORD`

Disse testene krever en separat database med navnet `husflid_test`.
Tabellene i testdatabasen slettes og opprettes på nytt ved teststart.
Testene skal derfor kjøres mot en egen testdatabase.

Se [Manuell testing](docs/manuell-kontroll.md) for sjekklisten
for testing i nettleseren.

## API

| Metode | Endepunkt | Resultat |
| --- | --- | --- |
| GET | `/api/health` | Serverstatus |
| GET | `/api/kurs` | Alle publiserte kurs |
| GET | `/api/kurs/kommende` | Publiserte kurs fra og med dagens dato, sortert etter dato |
| GET | `/api/kurs/{id}` | Detaljer om ett publisert kurs |

API-et bruker følgende feilkoder:

- `400` ved ugyldig kurs-ID.
- `404` ved ukjent eller upublisert kurs.
- `503` ved feil under databaseoppslag.

`/api/health` kontrollerer at serveren svarer.
Endepunktet kontrollerer ikke databasetilkoblingen.

## Samarbeid

`main` er prosjektets felles hovedbranch.

Oppgaver utvikles i korte arbeidsbrancher, for eksempel
`feature/kursoversikt`. Endringer gjennomgås med pull request før
de merges til `main`.

Commiter skal ha korte meldinger som beskriver endringene de inneholder.
Velg filene som hører til oppgaven når du lager en commit.

## Dokumentasjon

- [Arkitektur og avhengigheter](docs/arkitektur-og-avhengigheter.md)
- [Manuell testing](docs/manuell-kontroll.md)

Sprintplaner, arbeidsfordeling, møtereferater og evalueringer føres
i gruppas levende prosjektdokumentasjon.