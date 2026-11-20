# Manuell kontroll

Testtilfellene M1–M12 følger tabell 7.3 i prosjektrapporten. Resultatene fra prosjektets gjennomføring står i kapittel 7.3, med skjermbilder i kapittel 6 og 7. Tabellen under kan brukes på nytt etter en endring. Fyll ut den siste kolonnen med dato, miljø og det du faktisk observerte.

For M5, M9, M10 og M11 brukes vanlig oppstart med database. For å kontrollere mottak i M5 trengs også SMTP og en testinnboks. Bruk fiktive deltakere og egne testkurs i et miljø der testdata kan endres. Demomodus er tilstrekkelig for å prøve navigasjonen og den offentlige påmeldingsflyten uten varig lagring.

| ID | Hva du gjør | Forventet resultat | Resultat ved ny kjøring |
| --- | --- | --- | --- |
| M1 | Åpne forsiden. Følg snarveiene til Kurs, Aktuelt og Kontakt. | Forsiden viser de nyeste nyhetene og kommende kurs. Snarveiene åpner riktig side. | |
| M2 | Åpne kursoversikten med et eldre og et kommende publisert kurs i testdataene. | Fra og med dagens dato vises kursene med nærmeste dato først. Et avsluttet kurs vises ikke i oversikten. | |
| M3 | Velg et lokallag og et tema, og nullstill filtrene. | Treffene passer begge valgene. Når filtrene nullstilles, vises alle kommende kurs. | |
| M4 | Åpne detaljsiden for et kurs. | Dato, tidspunkt, sted, beskrivelse, nivå, utstyr, pris, kontaktperson og ledige plasser stemmer med testkurset. | |
| M5 | Meld en testperson på et kurs med ledig plass. Kontroller kvitteringen og innboksen. | Deltakeren er påmeldt, én plass er brukt, og e-post og kvittering har samme referanse. | |
| M6 | Meld en annen testperson på et fullt kurs. | Skjemaet tilbyr venteliste. Kvitteringen viser ventelistestatus, og antall ledige plasser er fortsatt null. | |
| M7 | Åpne Aktuelt og velg en publisert nyhet. Prøv kopiering av lenken og åpne delingsvalget. | Riktig tekst og bilde vises. Den kopierte lenken åpner samme nyhet. Delingsvalget åpner deling for nyheten. | |
| M8 | Åpne Kontakt og Ofte stilte spørsmål. | Kontaktsiden viser valgte kontaktpersoner. FAQ viser svar om blant annet avlysning, sykdom, betaling og påmelding. | |
| M9 | Prøv feil passord og deretter gyldig innlogging. Logg ut igjen. | Feil passord avvises. Gyldig konto får tilgang. Etter utlogging krever administrasjonen ny innlogging. | |
| M10 | Opprett og rediger en testnyhet og et testkurs. Publiser dem, og avlys deretter kurset. | Publiserte endringer blir synlige på nettsiden. Det avlyste kurset forsvinner fra den offentlige kurslisten. | |
| M11 | Åpne påmeldingsoversikten og velg et kurs med både påmeldte og ventende deltakere. | Kontaktopplysninger og status vises for riktig kurs. Med lokallagskonto vises bare egne deltakerlister. | |
| M12 | Åpne nettstedet i mobil-, nettbrett- og PC-bredde. Bruk menyen og gå gjennom en påmelding. | Innholdet tilpasser seg bredden, menyen virker, og hovedflyten kan brukes uten sidelengs rulling. | |

Ved kontroll av kø og kapasitet kan M6 og M11 prøves sammen. Meld av en deltaker med plass og kontroller at raden står som avmeldt. Velg deretter en ventende deltaker og trykk «Gi plass». Først da skal den ledige plassen tas i bruk.

Supplerende kontroller fra utviklingen:

- Åpne `/api/kurs/abc` og et kursnummer som ikke finnes. Forvent henholdsvis 400 og 404.
- Åpne kurssiden med og uten databasetilkobling i et lokalt testoppsett. Ved databasefeil skal API-et svare 503, og nettsiden skal vise en forståelig melding. Gjenopprett tilkoblingen og prøv igjen.
- Følg en lenke med Tab og aktiver den med Enter. Kontroller synlig fokus på mobilmenyen og i påmeldingsskjemaet.

Ved avvik, noter test-ID, nettleser, skjermbredde hvis relevant, forventet og faktisk resultat. Bruk testopplysninger i skjermbilder.
