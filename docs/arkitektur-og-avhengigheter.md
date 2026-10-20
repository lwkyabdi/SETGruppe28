# Arkitektur og avhengigheter

## Oppbygning

Javalin serverer både frontendfilene og API-et. Nettleseren bruker Vue til å
hente og vise kurs fra API-et. Backend er delt i controller, service og repository.
Repository-laget henter data fra MySQL med JDBC.

Frontend og API bruker samme adresse, lokalt `http://localhost:8080`.
Vue lastes fra WebJars og brukes direkte i HTML-filene.

| Område | Ansvar |
| --- | --- |
| `Application.java` | Setter sammen objekter, registrerer ruter og starter serveren |
| `config` | Leser databasekonfigurasjon og åpner forbindelser |
| `controller` | Håndterer HTTP-forespørsler og statuskoder |
| `service` | Behandler kursoppslag og filtrerer kommende kurs |
| `repository` | Henter kurs fra MySQL, eller fra eksempeldata ved oppstart med `--demo` |
| `model` | Inneholder Kurs og Kontaktperson |
| `dto` | Definerer JSON-formatet til API-et |
| `src/main/resources/public` | HTML, CSS, JavaScript og bilder |
| `database` | Tabellstruktur og eksempeldata |
| `src/test` | Service-, API- og databasetester |

`site.js` inneholder gjenbrukbare komponenter for toppmeny, bunntekst og kurskort,
samt API-kall, søk og filtre. `style.css` inneholder hovedstilen, og `site.css`
inneholder stiler for statusmeldinger, kontaktkort og Vue-visningene.

## Avhengigheter

Versjonene er fastsatt i `pom.xml`.

| Avhengighet | Versjon | Scope | Formål |
| --- | --- | --- | --- |
| `io.javalin:javalin` | 7.2.3 | compile | HTTP-server, ruter og statiske filer |
| `com.fasterxml.jackson.core:jackson-databind` | 2.20.1 | compile | JSON-svar |
| `org.slf4j:slf4j-simple` | 2.0.17 | compile | Logging |
| `com.mysql:mysql-connector-j` | 9.7.0 | runtime | JDBC-driver for MySQL |
| `org.webjars.npm:vue` | 3.5.41 | compile | Vue i nettleseren |
| `org.junit.jupiter:junit-jupiter` | 5.13.4 | test | Automatiserte tester |

Koden kompileres for Java 17 og kan kjøres med JDK 21.
Maven Wrapper bruker Maven 3.9.11. Vue bruker den ferdigbygde global-versjonen;
øvrige transitive npm-pakker fra WebJar-avhengigheten er ekskludert.

## Database

`kurs` har en fremmednøkkel til `kontaktperson`. API-et henter bare kurs med
status `PUBLISERT`. Detaljoppslag bruker `PreparedStatement` med parameterbundet ID.
Kommende kurs har dato fra og med dagens dato i tidssonen `Europe/Oslo`.

Applikasjonen leser data. Påmelding og andre skriveoperasjoner er ikke implementert.
Antall ledige plasser er foreløpig en lagret verdi.

Lokale innstillinger leses fra `dbConfig.properties`. Miljøvariablene `DB_URL`,
`DB_USER` og `DB_PASSWORD` har forrang når de er satt.

`--demo` velger `DemoKursRepository`, som lagrer fem eksempelkurser i minnet.
Vanlig oppstart velger `JdbcKursRepository` og krever databasekonfigurasjon.
