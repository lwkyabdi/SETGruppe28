# Østfold Husflidslag – Gruppe 28

Nettside for kurs og aktiviteter i Østfold Husflidslag.
Prosjekt i Software Engineering og Testing ved Høgskolen i Østfold.

## Teknologi

- Frontend: HTML, CSS, JavaScript og Vue.
- Backend: Java og Javalin.
- Database: MySQL.
- Bygg og tester: Maven Wrapper og JUnit.

## Funksjoner

- Forside med kommende kurs og nyheter.
- Kursoversikt med søk og filtrering på tema og lokallag.
- Kursdetaljer med dato, sted, pris, utstyr og kontaktperson.
- Kontaktoversikt og navigasjon tilpasset mobil.

Ved vanlig oppstart hentes kurs fra MySQL. Nyhetene ligger foreløpig som statiske eksempeldata
i JavaScript. Innlogging, administrasjon og digital påmelding er ikke implementert.
SQL-filen inneholder eksempeldata for lokal utvikling.

## Oppstart uten database

Forutsetning: JDK 21. Maven lastes ned automatisk av Maven Wrapper.
Kommandoene kjøres fra prosjektmappen i PowerShell.

```powershell
.\mvnw.cmd compile exec:java "-Dexec.args=--demo"
```

Åpne [localhost:8080](http://localhost:8080). Denne modusen bruker eksempelkurser
i minnet og trenger ingen databasekonfigurasjon. Kursdatoene beregnes fra dagens
dato ved oppstart. Dataene opprettes på nytt ved hver oppstart.

## Oppstart med database

1. Bruk en MySQL-database for prosjektet. For en database på HiØ brukes
   serveradresse, port, databasenavn og tilgangsopplysninger fra HiØ.
   Tilkobling utenfor campus kan kreve VPN eller SSH-tunnel.

2. Kjør `database/01-schema.sql` og deretter `database/02-seed.sql` i prosjektets
   utviklingsdatabase. Eksempeldataene importeres én gang i en tom database.

3. Kopier databasekonfigurasjonen:

   ```powershell
   Copy-Item dbConfig.example.properties dbConfig.properties
   ```

   Åpne `dbConfig.properties`, og tilpass `db.url`, `db.username` og `db.password`
   til databasen. Eksempelfilens `localhost` erstattes med faktisk serveradresse
   ved ekstern database. Tilkoblingsvalg og TLS settes etter serverens krav.
   Filen er utelatt fra Git.

4. Start applikasjonen:

   ```powershell
   .\mvnw.cmd compile exec:java
   ```

5. Åpne [localhost:8080](http://localhost:8080). Stopp serveren med **Ctrl + C**.

Vanlig oppstart bruker MySQL. Eksempeldata i minnet brukes bare med `--demo`.
På macOS og Linux brukes `sh mvnw` i stedet for `.\mvnw.cmd`.
Frontend og backend kjøres av samme server på port 8080.

## Tester

```powershell
.\mvnw.cmd test
```

Service-, API- og demo-testene kan kjøres uten MySQL.
MySQL-testene aktiveres med miljøvariablene `MYSQL_TEST_URL`, `MYSQL_TEST_USER`
og `MYSQL_TEST_PASSWORD`. De krever en separat database med navnet
`husflid_test`, der tabellene slettes og opprettes på nytt ved teststart.

## API

| Metode | Endepunkt | Resultat |
| --- | --- | --- |
| GET | `/api/health` | Serverstatus |
| GET | `/api/kurs` | Publiserte kurs |
| GET | `/api/kurs/kommende` | Publiserte kurs fra og med dagens dato, sortert etter dato |
| GET | `/api/kurs/{id}` | Detaljer om ett publisert kurs |

Ugyldig kurs-ID gir 400, ukjent eller upublisert kurs gir 404,
og feil ved databaseoppslag gir 503. Statusendepunktet kontrollerer bare serveren.

## Samarbeid

`main` er felles hovedbranch. Oppgaver utvikles i korte arbeidsbrancher,
for eksempel `feature/kursoversikt`, og gjennomgås med pull request til `main`.

## Dokumentasjon

- [Arkitektur og avhengigheter](docs/arkitektur-og-avhengigheter.md)
- [Manuell testing](docs/manuell-kontroll.md)
