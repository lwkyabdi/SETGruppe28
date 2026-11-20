# Østfold Husflidslag – Gruppe 28

Nettside for Østfold Husflidslag, utviklet i Software Engineering og Testing ved Høgskolen i Østfold. Besøkende kan finne kurs, melde seg på, lese nyheter og finne kontaktpersoner. Administratorene kan vedlikeholde kurs og nyheter og følge opp påmeldte og ventelister.

## Kjør prosjektet

Du trenger JDK 17 eller nyere. Kjør kommandoene fra prosjektmappen. Maven Wrapper henter Maven og avhengighetene ved første kjøring.

~~~powershell
git clone https://github.com/lwkyabdi/SETGruppe28.git
cd SETGruppe28
~~~

### Med eksempeldata

~~~powershell
.\mvnw.cmd compile exec:java "-Dexec.args=--demo"
~~~

Åpne [localhost:8080](http://localhost:8080/). I demomodus kan du prøve de offentlige sidene og påmeldingen uten database. Påmeldingene ligger i minnet til serveren stoppes, og e-postbekreftelser vises i terminalen. Innlogging og administrasjon krever vanlig oppstart.

### Med database og administrasjon

Bruk HiØ-nettet eller koble til HiØ-VPN utenfor campus. Applikasjonen kobler seg til MariaDB-skjemaet `se26_G28` med MySQL Connector/J.

~~~powershell
Copy-Item dbConfig.example.properties dbConfig.properties
~~~

Fyll inn databaseopplysningene i kopien. Du kan også sette `DB_URL`, `DB_USER` og `DB_PASSWORD`. En miljøvariabel med verdi overstyrer tilsvarende nøkkel i filen.

~~~powershell
.\mvnw.cmd compile exec:java
~~~

Åpne [nettsiden](http://localhost:8080/) eller [administrasjonen](http://localhost:8080/minside/admin.html). Du kan også kjøre `Application.main` fra IDE-en. På macOS og Linux brukes `sh ./mvnw` i stedet for `.\mvnw.cmd`. Stopp serveren med Ctrl+C.

**E-post.** For å sende e-post med SMTP, kopier `epostConfig.example.properties` til `epostConfig.properties` og fyll inn `smtp.host`, `smtp.port`, `smtp.username` og `smtp.password` før oppstart. Uten filen skrives bekreftelsen i terminalen. Kvitteringen i nettleseren viser at påmeldingen er registrert, også hvis utsendingen feiler. Database- og e-postpassord hører hjemme i den lokale konfigurasjonen.

**Administratorer.** En administrator med rollen `LOKALLAG` administrerer sitt eget lag. Rollen `ADMIN` har tilgang til alle lagene. Kontoene ligger i tabellen `administrator`. For å opprette en konto, kjør `LagPassordHash.main` fra testkoden og lagre hashen i `passord_hash`. Angi `lokallag_id` for en lokallagskonto og `NULL` for en fylkeskonto.

## Databasen

En vanlig oppstart bruker det eksisterende skjemaet. SQL-filene kjøres separat. Ved oppsett av en tom test- eller demonstrasjonsdatabase kjøres filene i `database/` i nummerrekkefølge fra 01 til 09. Fil 02 inneholder eksempeldata med faste ID-er og er beregnet på en tom database. Fil 07 og 09 kontrollerer at valgt skjema er `se26_G28` eller `husflid_test`. På en eksisterende installasjon kjøres de migreringene som mangler.

- 07 legger til kursstatusen `AVLYST`.
- 08 legger til kønummer, tidspunkt for avmelding og en unik nøkkel for aktive påmeldinger.
- 09 fjerner de tre ubrukte tabellene `aktivitet`, `kontaktmelding` og `medlemsinteresse`. Skriptet avbryter hvis noen av dem inneholder rader. Etter migrering 09 har skjemaet åtte tabeller og seks fremmednøkler.

Et fullt kurs gir en plass på ventelisten. Den vanlige påmeldingsflyten lagrer denne i `pamelding` med status `VENTELISTE`. Administratoren bruker «Gi plass» når en plass blir ledig. Avmeldinger beholdes i historikken, og applikasjonen sperrer en ny påmelding til samme kurs med samme e-postadresse også etter avmelding.

Automatisk opprykk fra ventelisten og automatisk sletting av deltakeropplysninger etter ett år er videre arbeid. Betaling skjer utenfor nettstedet. Lokallagene må avklare vilkårene for avlysning og refusjon før drift.

## Prosjektfiler

| Mappe eller fil | Innhold |
| --- | --- |
| `src/main/java/no/hiof/gruppe28/` | Javalin-server, tjenester, JDBC-repositoryer og e-post |
| `src/main/resources/public/` | Offentlige HTML-sider, `script.js`, stilark og bilder |
| `src/main/resources/public/minside/` | Administrasjonssidene med `admin.js` og `admin.css` |
| `opplastinger/` | Bilder lastet opp fra administrasjonen, lagret på serveren |
| `database/` | SQL-skjema, eksempeldata og migreringer |
| `src/test/java/` | Enhets-, API- og databasetester |
| `pom.xml` | Avhengigheter og byggeinnstillinger |

[Arkitektur og avhengigheter](docs/arkitektur-og-avhengigheter.md) beskriver dataflyten og bibliotekene.

## Tester

~~~powershell
.\mvnw.cmd clean test
~~~

Enhets- og API-testene kan kjøres uten skolens database. For JDBC-testene oppretter du først en separat, tom MariaDB-database med navnet `husflid_test`. Testbrukeren må kunne opprette, endre og slette tabeller der. Testene bygger opp skjemaet på nytt. Sett disse miljøvariablene før samme testkommando kjøres:

| Variabel | Verdi |
| --- | --- |
| `MYSQL_TEST_URL` | For eksempel `jdbc:mysql://127.0.0.1:3306/husflid_test` |
| `MYSQL_TEST_USER` | Brukernavn til testdatabasen |
| `MYSQL_TEST_PASSWORD` | Passord til testbrukeren |

Uten `MYSQL_TEST_URL` hopper JUnit over JDBC-testene. [Manuell kontroll](docs/manuell-kontroll.md) følger testtilfellene M1–M12 i rapporten.

## API

API-et svarer med JSON. Feilsvar bruker feltet `melding`.

| Metode | Offentlig adresse | Formål |
| --- | --- | --- |
| GET | `/api/health` | Sjekke at serveren kjører |
| GET | `/api/kurs` | Alle publiserte kurs, også tidligere kurs |
| GET | `/api/kurs/kommende` | Publiserte kurs fra og med i dag, sortert på dato |
| GET | `/api/kurs/{id}` | Ett publisert kurs med kontaktperson |
| POST | `/api/kurs/{id}/pameldinger` | Registrere påmelding eller venteliste |
| GET | `/api/lokallag`, `/api/kontaktpersoner` | Lokallag og synlige kontaktpersoner |
| GET | `/api/nyheter`, `/api/nyheter/{id}` | Publiserte nyheter |

| Metode | Innlogging og administrasjon | Formål |
| --- | --- | --- |
| POST | `/api/login`, `/api/logout` | Logge inn eller ut |
| GET | `/api/meg` | Innlogget bruker |
| GET | `/api/admin/oversikt`, `/api/admin/pameldinger` | Nøkkeltall og deltakerliste |
| POST | `/api/admin/kurs/{id}/pameldinger` | Registrere en påmelding på vegne av en deltaker |
| POST | `/api/admin/pameldinger/{referanse}/gi-plass` | Gi en ventende deltaker plass |
| DELETE | `/api/admin/pameldinger/{referanse}` | Melde av og beholde historikken |
| GET, POST | `/api/admin/kurs`, `/api/admin/nyheter` | Hente oversikt eller opprette innhold |
| GET, PUT, DELETE | `/api/admin/kurs/{id}`, `/api/admin/nyheter/{id}` | Hente, oppdatere eller slette ett kurs eller én nyhet |
| GET | `/api/admin/kontaktpersoner` | Hente kontaktpersoner til kursadministrasjonen |
| POST | `/api/admin/bilder` | Laste opp et bilde |

Ugyldige data gir normalt 400, manglende innlogging 401 og ukjente ressurser 404. Konflikter som en duplikatpåmelding eller forsøk på å gi plass på et fullt kurs gir 409. En databasefeil gir 503, og en uventet serverfeil gir 500.
