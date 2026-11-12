# Østfold Husflidslag – Gruppe 28

Dette er nettsiden vi har laget for Østfold Husflidslag i emnet Software Engineering og Testing ved HiØ.

På nettsiden kan man finne kurs, melde seg på, lese nyheter og finne kontaktinfo. Lokallagene kan logge inn og legge inn egne kurs og nyheter, og se hvem som har meldt seg på.

## Hva vi har brukt

- HTML, CSS og JavaScript til nettsiden
- Java og Javalin til backend
- MySQL-databasen på HiØ sin server
- Maven til å bygge prosjektet
- JUnit til testene
- Git og GitHub til samarbeidet

## Hva nettsiden kan

**Besøkende kan:**

- se kommende kurs, og søke og filtrere på tema og lokallag
- se detaljer om et kurs, med ledige plasser og kontaktperson
- melde seg på et kurs, eller på venteliste hvis det er fullt
- få bekreftelse på e-post
- lese nyheter og se kontaktpersoner

**Administratorer kan:**

- logge inn og ut
- se påmeldte, legge til nye, gi plass fra ventelisten og slette påmeldinger
- lage, endre og slette kurs og nyheter, og laste opp bilder
- bare se og endre sitt eget lokallag sine ting

## Mappene i prosjektet

| Mappe / fil | Hva som ligger der |
| --- | --- |
| `src/main/java/` | Backend |
| `src/main/resources/public/` | HTML, CSS, JavaScript og bilder |
| `src/main/resources/public/minside/` | Admin-sidene |
| `src/test/java/` | Testene |
| `database/` | SQL-filene til databasen |
| `docs/` | Manuell testing |
| `pom.xml` | Bibliotekene vi bruker |

## Slik starter du prosjektet

Du trenger Git, JDK 17 eller nyere, og HiØ-VPN for å komme til databasen.

**1. Hent prosjektet**

```powershell
git clone https://github.com/lwkyabdi/SETGruppe28.git
cd SETGruppe28
```

**2. Koble til databasen**

Databasen heter `se26_G28` og ligger på `itstud.hiof.no`. Lag en kopi av oppsettfilen:

```powershell
Copy-Item dbConfig.example.properties dbConfig.properties
```

Åpne `dbConfig.properties` og skriv inn passordet:

```properties
db.url=jdbc:mysql://itstud.hiof.no:3306/se26_G28?connectionTimeZone=Europe/Oslo
db.username=gruppe28
db.password=FYLL_INN_DATABASEPASSORD
```

Denne filen skal aldri pushes til GitHub, siden passordet står i den.

Hvis databasen må lages på nytt, kjører du SQL-filene i `database/` fra `01` til `06`.

**3. Start serveren**

```powershell
.\mvnw.cmd compile exec:java
```

Gå til [localhost:8080](http://localhost:8080/). Stopp serveren med Ctrl + C.

Vil du teste uten database, kan du starte med eksempeldata i stedet:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.args=--demo"
```

Da virker ikke innlogging og admin.

## Admin

Admin-siden ligger på [localhost:8080/minside/admin.html](http://localhost:8080/minside/admin.html).

Administratorene ligger i tabellen `administrator`. Vi lagrer aldri passordet direkte, bare en hash av det. For å lage en ny administrator:

1. Kjør `LagPassordHash` (ligger i `src/test/java/`), skriv inn passordet og kopier hashen du får.
2. Legg inn en ny rad i `administrator` med hashen i `passord_hash`. Rollen er `ADMIN` for hele fylket, eller `LOKALLAG` med id-en til laget.

## Tester

```powershell
.\mvnw.cmd test
```

Databasetestene hoppes over hvis testdatabasen `husflid_test` ikke er satt opp. Manuell testing i nettleseren står i [docs/manuell-kontroll.md](docs/manuell-kontroll.md).

## API

Backend svarer med JSON. Hvis noe går galt, får man `{"melding": "..."}`.

**Alle kan bruke disse:**

| Metode | Adresse | Hva den gjør |
| --- | --- | --- |
| GET | `/api/health` | Sjekker at serveren kjører |
| GET | `/api/kurs` | Alle publiserte kurs |
| GET | `/api/kurs/kommende` | Kurs fra i dag og framover |
| GET | `/api/kurs/{id}` | Ett kurs |
| POST | `/api/kurs/{id}/pameldinger` | Melder seg på et kurs |
| GET | `/api/lokallag` | Lokallagene |
| GET | `/api/kontaktpersoner` | Kontaktpersonene |
| GET | `/api/nyheter` | Nyhetene |
| GET | `/api/nyheter/{id}` | Én nyhet |
| POST | `/api/login` | Logger inn |
| POST | `/api/logout` | Logger ut |
| GET | `/api/meg` | Hvem som er logget inn |

**Bare for administratorer:**

| Metode | Adresse | Hva den gjør |
| --- | --- | --- |
| GET | `/api/admin/oversikt` | Antall kurs, påmeldte og på venteliste |
| GET | `/api/admin/pameldinger` | Påmeldte og venteliste |
| POST | `/api/admin/pameldinger/{referanse}/gi-plass` | Gir plass til en på ventelisten |
| DELETE | `/api/admin/pameldinger/{referanse}` | Sletter en påmelding |
| POST | `/api/admin/kurs/{id}/pameldinger` | Legger til en påmelding |
| GET, POST | `/api/admin/kurs` | Henter kurs og lager nye |
| GET, PUT, DELETE | `/api/admin/kurs/{id}` | Henter, endrer og sletter et kurs |
| GET, POST | `/api/admin/nyheter` | Henter nyheter og lager nye |
| GET, PUT, DELETE | `/api/admin/nyheter/{id}` | Henter, endrer og sletter en nyhet |
| GET | `/api/admin/kontaktpersoner` | Alle kontaktpersonene |
| POST | `/api/admin/bilder` | Laster opp et bilde |

**Feilkodene vi bruker:**

| Kode | Betyr |
| --- | --- |
| 400 | Noe er feil i det som ble sendt |
| 401 | Du er ikke logget inn |
| 404 | Finnes ikke |
| 409 | Går ikke, for eksempel fordi kurset er fullt |
| 500 | Noe gikk galt på serveren |
| 503 | Databasen svarer ikke |

## Hvordan vi jobbet med Git

Hver oppgave fikk sin egen branch. Når den var ferdig, lagde vi en pull request og merget den inn i `main`. Vi kjørte alle testene før vi merget.
