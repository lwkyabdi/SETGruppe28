# Manuell testing

Datoene i denne malen er fiktive og dokumenterer ikke gjennomførte tester.
Faktisk resultat, tester og korrekt dato registreres ved gjennomføring.

Navigasjon, søk og utseende kan prøves med `--demo`. Kontroll av databaseinnhold,
brutt databasetilkobling og nye SQL-rader krever vanlig oppstart med MySQL.

| Nr. | Handling | Forventet resultat | Faktisk resultat / tester / dato |
| --- | --- | --- | --- |
| 1 | Start appen og åpne forsiden | Siste nytt og kommende kurs vises | 09.10.2026 |
| 2 | Sammenlign datoene med MySQL | Bare kurs fra og med i dag vises under kommende | 09.10.2026 |
| 3 | Åpne kursoversikten | Alle publiserte kurs vises; kladd ID 8 er skjult | 09.10.2026 |
| 4 | Søk på bunad, deretter en tekst uten treff | Riktig kurs, deretter en forståelig tomtilstand | 09.10.2026 |
| 5 | Filtrer på Moss og Veving | Bare passende kurs vises | 09.10.2026 |
| 6 | Åpne kurs ID 1 og ID 2 | Ulike titler, priser, steder og detaljer | 09.10.2026 |
| 7 | Åpne /api/kurs/99999 | HTTP 404 | 10.10.2026 |
| 8 | Åpne /api/kurs/8 | HTTP 404 for kladd | 10.10.2026 |
| 9 | Åpne /api/kurs/abc | HTTP 400 | 10.10.2026 |
| 10 | Åpne kurs-detalj.html?id=99999 | Forklarende melding, tilbakevei til kursoversikt | 10.10.2026 |
| 11 | Åpne kontakt og kurskontakt | Navn, bildeillustrasjon, e-post og telefon vises | 10.10.2026 |
| 12 | Åpne de tre nyhetene | Riktig eksempelnyhet vises; ukjent ID gir melding | 10.10.2026 |
| 13 | Prøv alle lenker i topp, bunn og kort | Riktig side åpnes | 10.10.2026 |
| 14 | Prøv 390 px, 768 px og PC-bredde | Lesbar tekst, meny kan brukes, ingen blokkert hovedflyt | 11.10.2026 |
| 15 | Bruk Tab/Shift+Tab og Enter | Synlig fokus og brukbar navigasjon | 11.10.2026 |
| 16 | Se Network i nettleserens utviklerverktøy | Kurs hentes fra /api; ingen feilede nødvendige ressurser | 11.10.2026 |
| 17 | Simuler utilgjengelig database gjennom en midlertidig feil i den lokale appens tilkobling, og last kurssiden på nytt | API-et svarer med HTTP 503; siden viser en forståelig feilmelding og ingen falske kursdata | 11.10.2026 |
| 18 | Gjenopprett riktig databasetilkobling, start appen på nytt og trykk Prøv igjen | Data lastes uten å endre kildekoden | 11.10.2026 |
| 19 | Legg til et syntetisk kurs i MySQL og last siden på nytt | Nytt publisert kurs vises; dataflyten er demonstrert | 12.10.2026 |
| 20 | La et annet gruppemedlem følge README fra ren kloning | Samme funksjoner kan startes på annen maskin | 12.10.2026 |

Test av utilgjengelig database utføres ved å endre tilkoblingen i den lokale appen
midlertidig. Skolens databaseserver skal fortsette å kjøre.
Gjenopprett de riktige tilkoblingsinnstillingene etter testen.

Tilfelle 19 kan gjøres ved å kopiere en eksempelrad med ny ID i Workbench;
kontroller alle påkrevde felt og en eksisterende kontakt-ID.
Fjern testkurset etter gjennomført kontroll.

Feil registreres som Issues med gjenskapingssteg og forventet og faktisk resultat.