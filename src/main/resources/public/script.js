//MENY PÅ MOBIL
const menyknapp = document.querySelector(".menyknapp");
const mobilmeny = document.getElementById("mobilmeny");

menyknapp.addEventListener("click", function () {
  const erApen = menyknapp.getAttribute("aria-expanded") === "true";

  menyknapp.setAttribute("aria-expanded", String(!erApen));
  menyknapp.setAttribute("aria-label", erApen ? "Åpne meny" : "Lukk meny");
  mobilmeny.hidden = erApen;
});

//SØK OG FILTER PÅ KURSOVERSIKTEN
//Kjører bare på kurs.html
const kursfilter = document.querySelector(".kursfilter");

if (kursfilter) {
  const sokefelt = document.getElementById("kurs-sok");
  const temaValg = document.getElementById("tema-filter");
  const lokallagValg = document.getElementById("lokallag-filter");
  const kurs = document.querySelectorAll("#kursliste > li");
  const antall = document.getElementById("antall-kurs");
  const ingenTreff = document.getElementById("ingen-treff");

  //Hent valg fra adressen (f.eks. ?lokallag=moss)
  const adresse = new URLSearchParams(window.location.search);
  sokefelt.value = adresse.get("sok") || "";
  temaValg.value = adresse.get("tema") || "";
  lokallagValg.value = adresse.get("lokallag") || "";

  //Vis bare kursene som passer
  function filtrerKurs() {
    const sok = sokefelt.value.trim().toLowerCase();
    const tema = temaValg.value;
    const lokallag = lokallagValg.value;
    let synlige = 0;

    kurs.forEach(function (li) {
      const passerSok = li.textContent.toLowerCase().includes(sok);
      const passerTema = tema === "" || li.dataset.tema === tema;
      const passerLokallag = lokallag === "" || li.dataset.lokallag === lokallag;
      const vis = passerSok && passerTema && passerLokallag;

      li.hidden = !vis;
      if (vis) synlige++;
    });

    antall.textContent = synlige;
    ingenTreff.hidden = synlige > 0;
  }

  //Filtrer mens man skriver eller velger
  kursfilter.addEventListener("input", filtrerKurs);
  kursfilter.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();
    filtrerKurs();
  });

  //Filtrer når siden lastes
  filtrerKurs();
}

//KURSENE
//Demo-data for de seks kursene på kursoversikten. Erstattes av GET /api/kurs/{id} senere.
//ledige = antall ledige plasser. Strikkekurset (id 2) er fullt, så det viser ventelisten.
const KURS = {
  1: { tittel: "Bunadssøm for nybegynnere", tema: "Bunad", lokallag: "Fredrikstad",
    dato: "2024-09-20", tid: "18:00–21:00", varighet: "8 kvelder", sted: "Fredrikstad Husflidssenter, Nygårdsgt. 14",
    kursleder: "Marit Andersen", malgruppe: "Voksne", niva: "Nybegynner", plasser: 8, ledige: 3, medlemspris: 1500, ordinaerpris: 2000,
    beskrivelse: "Lær å sy din egen bunad fra bunnen av. Kurset passer deg som aldri har sydd bunad før. Vi går gjennom alt fra valg av stoff til ferdig plagg. Du vil lære tradisjonelle syteknikker og bli kjent med bunadens kulturhistorie i Østfold.",
    utstyr: "Symaskin hvis du har, stoff og tråd." },
  2: { tittel: "Strikk din egen genser", tema: "Strikking", lokallag: "Sarpsborg",
    dato: "2024-09-27", tid: "10:00–14:00", varighet: "6 lørdager", sted: "Sarpsborg Husflidslag, Glengsgata 15",
    kursleder: "Anne Berg", malgruppe: "Voksne", niva: "Litt erfaren", plasser: 8, ledige: 0, medlemspris: 1200, ordinaerpris: 1600,
    beskrivelse: "Strikk en klassisk genser i ditt eget tempo. Du lærer å lese oppskrift, måle og strikke sammen, med god hjelp fra erfarne strikkere.",
    utstyr: "Rundpinner nr. 3,5 og 4. Garn kan kjøpes på kurset." },
  3: { tittel: "Vevkurs – grunnleggende båndbinding", tema: "Veving", lokallag: "Moss",
    dato: "2024-10-05", tid: "10:00–15:00", varighet: "2 dager", sted: "Moss Husflidslag, vevstudioet",
    kursleder: "Liv Johansen", malgruppe: "Alle", niva: "Nybegynner", plasser: 10, ledige: 6, medlemspris: 900, ordinaerpris: 1200,
    beskrivelse: "Lær grunnleggende båndveving på bandgrind. Kurset passer for nybegynnere, og du går hjem med ditt eget bånd.",
    utstyr: "Alt utstyr finnes i vevstudioet." },
  4: { tittel: "Broderi og korssting – nybegynnerkurs", tema: "Brodering", lokallag: "Halden",
    dato: "2024-10-12", tid: "13:00–16:00", varighet: "4 lørdager", sted: "Halden Husflidslag",
    kursleder: "Eva Karlsen", malgruppe: "Alle", niva: "Nybegynner", plasser: 10, ledige: 8, medlemspris: 800, ordinaerpris: 1100,
    beskrivelse: "Lær korssting og enkle broderisting. Du lager en liten løper med tradisjonelle mønstre fra Østfold.",
    utstyr: "Broderisaks. Stoff og garn får du på kurset." },
  5: { tittel: "Trearbeid – tradisjonell skjæring", tema: "Trearbeid", lokallag: "Fredrikstad",
    dato: "2024-10-19", tid: "09:00–15:00", varighet: "1 hel lørdag", sted: "Fredrikstad Husflidssenter, Nygårdsgt. 14",
    kursleder: "Per Olsen", malgruppe: "Voksne", niva: "Nybegynner", plasser: 6, ledige: 2, medlemspris: 1100, ordinaerpris: 1500,
    beskrivelse: "Lær å skjære i tre med kniv og huljern. Du lager en smørkniv eller en liten skål i bjørk.",
    utstyr: "Arbeidsklær. Verktøy kan lånes." },
  6: { tittel: "Søm og enkel konfeksjon", tema: "Søm", lokallag: "Sarpsborg",
    dato: "2024-11-02", tid: "10:00–14:00", varighet: "5 lørdager", sted: "Sarpsborg Husflidslag, Glengsgata 15",
    kursleder: "Anne Berg", malgruppe: "Voksne", niva: "Nybegynner", plasser: 8, ledige: 5, medlemspris: 1100, ordinaerpris: 1500,
    beskrivelse: "Lær å bruke symaskin og sy enkle plagg etter mønster, som et skjørt eller en pute.",
    utstyr: "Symaskin hvis du har. Stoff kjøper du selv." }
};

//Finner kurset ut fra adressen (?id= eller ?kurs=). Uten nummer brukes kurs 1.
function hentKursId(reserve) {
  const adresse = new URLSearchParams(window.location.search);
  const id = Number(adresse.get("id") || adresse.get("kurs") || reserve || 1);
  return KURS[id] ? id : 1;
}

//Fyller alle elementer med data-kurs="..." med opplysninger om kurset
function fyllKurs(kurs) {
  const kr = function (tall) { return tall.toLocaleString("nb-NO") + " kr"; };
  const verdier = {
    tittel: kurs.tittel, tema: kurs.tema, lokallag: kurs.lokallag, lagnavn: kurs.lokallag + " Husflidslag",
    dato: new Date(kurs.dato + "T12:00").toLocaleDateString("nb-NO", { day: "numeric", month: "long", year: "numeric" }),
    tid: kurs.tid, varighet: kurs.varighet, sted: kurs.sted, kursleder: kurs.kursleder,
    malgruppe: kurs.malgruppe, niva: kurs.niva, beskrivelse: kurs.beskrivelse, utstyr: kurs.utstyr,
    plasser: kurs.ledige === 0 ? "Fullt" : kurs.ledige + " av " + kurs.plasser,
    plasserLedige: kurs.ledige === 0 ? "Fullt (" + kurs.plasser + " plasser)" : kurs.ledige + " av " + kurs.plasser + " ledige",
    medlemspris: kr(kurs.medlemspris), ordinaerpris: kr(kurs.ordinaerpris)
  };
  document.querySelectorAll("[data-kurs]").forEach(function (felt) {
    felt.textContent = verdier[felt.dataset.kurs];
    if (felt.dataset.kurs === "dato") felt.dateTime = kurs.dato;
  });
}

//KURSSIDEN
//Kjører bare på kurs-detalj.html. Viser kurset fra ?id= i adressen.
//Er kurset fullt, blir «Meld deg på» til «Sett meg på venteliste».
const meldPaKnapp = document.getElementById("meld-pa-knapp");

if (meldPaKnapp) {
  const kursId = hentKursId();
  const kurs = KURS[kursId];
  fyllKurs(kurs);
  document.title = kurs.tittel + " – Østfold Husflidslag";
  document.querySelector(".brodsmuler [aria-current]").href = "kurs-detalj.html?id=" + kursId;

  //Plass-måleren øverst i påmeldingsboksen
  const maler = document.getElementById("ledige-plasser");
  maler.max = kurs.plasser;
  maler.value = kurs.ledige;
  maler.textContent = kurs.ledige + " av " + kurs.plasser;

  meldPaKnapp.href = "pamelding.html?kurs=" + kursId;

  if (kurs.ledige === 0) {
    //Klassen kurs-fullt endrer farger og utseende i style.css
    document.querySelector("main").classList.add("kurs-fullt");
    document.querySelectorAll(".fullt-merke, .fullt-etikett, .fullt-banner").forEach(function (felt) {
      felt.hidden = false;
    });

    //Stolpen viser at alle plassene er tatt
    document.querySelector(".ledige label").textContent = "Påmeldte";
    document.querySelector(".ledige span").textContent = kurs.plasser + " av " + kurs.plasser + " – fullt";
    maler.value = kurs.plasser;
    maler.textContent = "Fullt";

    meldPaKnapp.textContent = "Sett meg på venteliste";
    meldPaKnapp.href += "&venteliste=ja";
    document.getElementById("venteliste-info").hidden = false;

    //Forslag til kurs med ledige plasser, samme tema først (B7)
    const liste = document.getElementById("alternativ-liste");
    Object.keys(KURS)
      .map(Number)
      .filter(function (id) { return KURS[id].ledige > 0; })
      .sort(function (a, b) { return (KURS[b].tema === kurs.tema) - (KURS[a].tema === kurs.tema); })
      .slice(0, 3)
      .forEach(function (id) {
        const lenke = document.createElement("a");
        lenke.href = "kurs-detalj.html?id=" + id;
        const tittel = document.createElement("strong");
        tittel.textContent = KURS[id].tittel;
        const info = document.createElement("span");
        info.textContent = KURS[id].lokallag + " · " + KURS[id].ledige + " ledige plasser";
        lenke.append(tittel, info);
        const li = document.createElement("li");
        li.append(lenke);
        liste.append(li);
      });
    document.getElementById("ledige-alternativ").hidden = false;
  }
}

//PÅMELDING
//Kjører bare på pamelding.html. Kurset hentes fra Java.
const skjema = document.getElementById("pamelding-skjema");

if (skjema) {
  visPameldingsskjema();
}

async function visPameldingsskjema() {
  const feilmelding = document.getElementById("skjema-feil");
  const prisboks = document.querySelector(".din-pris");
  const dinPris = document.getElementById("din-pris");
  const medlemsnummer = document.querySelectorAll(".medlemsnummer");

  //Vis riktig pris og medlemsnummer når man velger medlemskap
  function oppdaterMedlemskap() {
    const valgt = skjema.querySelector('input[name="medlem"]:checked');
    if (!valgt) return;
    const erMedlem = valgt.value === "ja";

    dinPris.textContent = erMedlem ? prisboks.dataset.medlem : prisboks.dataset.ordinaer;
    medlemsnummer.forEach(function (felt) {
      felt.hidden = !erMedlem;
    });
  }

  //Fyll inn skjemaet igjen hvis man trykker "Tilbake" fra bekreftelsen.
  //En påmelding som allerede er bekreftet slettes, så skjemaet blir tomt.
  let lagret = hentPamelding();
  if (lagret && lagret.bekreftet) {
    slettPamelding();
    lagret = null;
  }

  //Kurset fra ?kurs= i adressen, eller fra påmeldingen hvis man kom tilbake
  const adresse = new URLSearchParams(window.location.search);
  const kursId = Number(adresse.get("kurs") || (lagret && lagret.kursId));
  const kurs = await hentKursFraApi(kursId);

  if (!kurs) {
    skjema.hidden = true;
    feilmelding.textContent = "Vi fant ikke kurset. Gå tilbake til kursoversikten og velg kurset på nytt.";
    feilmelding.hidden = false;
    skjema.after(feilmelding);

    //Skjul eksempelkurset i oppsummeringen, så det ikke ser ut som et ekte kurs
    const oppsummering = document.getElementById("oppsummering-tittel");
    if (oppsummering) oppsummering.closest("section, aside").hidden = true;

    const brodsmule = document.getElementById("brodsmule-kurs");
    brodsmule.textContent = "Ukjent kurs";
    brodsmule.href = "kurs.html";
    return;
  }

  fyllKurs(kurs);
  document.title = "Påmelding: " + kurs.tittel + " – Østfold Husflidslag";
  document.getElementById("brodsmule-kurs").href = "kurs-detalj.html?id=" + kursId;
  prisboks.dataset.medlem = kurs.medlemspris.toLocaleString("nb-NO") + " kr";
  prisboks.dataset.ordinaer = kurs.ordinaerpris.toLocaleString("nb-NO") + " kr";

  //Venteliste når kurset er fullt
  const erVenteliste = kurs.ledige === 0;

  if (erVenteliste) {
    document.getElementById("pamelding-tittel").textContent = "Sett deg på venteliste";
    document.getElementById("pamelding-ingress").textContent =
      "Kurset er fullt. Fyll ut skjemaet for å stå på ventelisten. Når noen melder seg av, får førstemann på ventelisten plassen og beskjed på e-post.";
    document.getElementById("venteliste-pris").hidden = false;
    document.title = "Venteliste: " + kurs.tittel + " – Østfold Husflidslag";
  }

  if (lagret) {
    ["fornavn", "etternavn", "epost", "telefon", "fodselsar", "medlemsnummer", "kommentar"].forEach(function (navn) {
      skjema.elements[navn].value = lagret[navn] || "";
    });
    const medlemValg = skjema.querySelector('input[name="medlem"][value="' + lagret.medlem + '"]');
    if (medlemValg) medlemValg.checked = true;
    skjema.elements.samtykke.checked = Boolean(lagret.samtykke);
    oppdaterMedlemskap();
  }

  skjema.addEventListener("change", function (hendelse) {
    if (hendelse.target.name === "medlem") oppdaterMedlemskap();
  });

  //Fjern rød ramme når feltet blir riktig
  skjema.addEventListener("input", function (hendelse) {
    if (hendelse.target.checkValidity()) {
      hendelse.target.removeAttribute("aria-invalid");
    }
  });

  //Sjekk skjemaet og gå videre til bekreftelsen
  skjema.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();

    //Finn feltene som mangler eller er feil
    const ugyldige = Array.from(skjema.elements).filter(function (felt) {
      return felt.willValidate && !felt.checkValidity();
    });

    skjema.querySelectorAll("[aria-invalid]").forEach(function (felt) {
      felt.removeAttribute("aria-invalid");
    });

    if (ugyldige.length > 0) {
      ugyldige.forEach(function (felt) {
        felt.setAttribute("aria-invalid", "true");
      });
      feilmelding.textContent = "Fyll ut alle feltene som mangler, og kryss av for samtykke.";
      feilmelding.hidden = false;
      ugyldige[0].focus();
      return;
    }

    //Lagre opplysningene til neste side, med en ny, unik nøkkel
    const data = Object.fromEntries(new FormData(skjema));
    data.samtykke = skjema.elements.samtykke.checked;
    data.pris = dinPris.textContent;
    data.venteliste = erVenteliste;
    data.kursId = kursId;
    data.nokkel = crypto.randomUUID();
    lagrePamelding(data);

    window.location.href = skjema.getAttribute("action");
  });
}

//BEKREFT PÅMELDING
//Kjører bare på bekreft.html. Sender påmeldingen til Java.
const bekreftSkjema = document.getElementById("bekreft-skjema");

if (bekreftSkjema) {
  visBekreftelse();
}

async function visBekreftelse() {
  const pamelding = hentPamelding();
  const knapp = document.getElementById("bekreft-knapp");

  if (!pamelding || !pamelding.nokkel) {
    //Ingen påmelding: vis melding i stedet for tomt kort
    bekreftSkjema.hidden = true;
    document.getElementById("ingen-pamelding").hidden = false;
    return;
  }

  //Feilmelding over knappen. Den finnes ikke i HTML-en, så vi lager den her.
  const feilmelding = document.createElement("p");
  feilmelding.className = "skjema-feil";
  feilmelding.setAttribute("role", "alert");
  feilmelding.hidden = true;
  knapp.before(feilmelding);

  //Verdiene som skal inn i feltene med data-felt
  const felter = {
    navn: pamelding.fornavn + " " + pamelding.etternavn,
    epost: pamelding.epost,
    telefon: pamelding.telefon,
    fodselsar: pamelding.fodselsar,
    medlemskap: pamelding.medlem === "ja" ? "Ja, jeg er medlem" : "Nei, jeg er ikke medlem",
    pris: pamelding.pris
  };

  bekreftSkjema.querySelectorAll("[data-felt]").forEach(function (felt) {
    felt.textContent = felter[felt.dataset.felt] || "–";
  });

  //Kurset personen melder seg på, hentet fra Java
  const kurs = await hentKursFraApi(pamelding.kursId);
  if (kurs) fyllKurs(kurs);
  document.querySelector("#tilbake a").href = "pamelding.html?kurs=" + pamelding.kursId;

  //Venteliste: si nøyaktig hva knappen gjør
  if (pamelding.venteliste) {
    document.getElementById("bekreft-tittel").textContent = "Bekreft plass på ventelisten";
    knapp.textContent = "Sett meg på venteliste";
    document.title = "Bekreft venteliste – Østfold Husflidslag";
  }

  //Send påmeldingen til Java. Knappen kan ikke trykkes to ganger mens vi venter.
  let sender = false;

  bekreftSkjema.addEventListener("submit", async function (hendelse) {
    hendelse.preventDefault();
    if (sender) return;
    sender = true;
    knapp.disabled = true;
    feilmelding.hidden = true;

    try {
      const svar = await fetch("/api/kurs/" + pamelding.kursId + "/pameldinger", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(tilPameldingJson(pamelding))
      });
      const innhold = await svar.json().catch(function () { return {}; });

      if (!svar.ok) {
        feilmelding.textContent = innhold.melding || "Påmeldingen kunne ikke lagres. Prøv igjen.";
        feilmelding.hidden = false;
        return;
      }

      //Lagret i Java: husk kvitteringen og gå videre
      pamelding.bekreftet = true;
      pamelding.kvittering = innhold;
      lagrePamelding(pamelding);
      window.location.href = bekreftSkjema.getAttribute("action");
    } catch (feil) {
      //Ingen kontakt. Samme nøkkel brukes neste gang, så påmeldingen lagres ikke to ganger.
      console.error(feil);
      feilmelding.textContent = "Fikk ikke kontakt med serveren. Sjekk nettet og trykk på knappen igjen.";
      feilmelding.hidden = false;
    } finally {
      sender = false;
      knapp.disabled = false;
    }
  });
}

//Bare feltene Java tar imot, med riktige typer
function tilPameldingJson(p) {
  return {
    nokkel: p.nokkel,
    fornavn: p.fornavn,
    etternavn: p.etternavn,
    epost: p.epost,
    telefon: p.telefon,
    fodselsar: Number.parseInt(p.fodselsar, 10),
    medlem: p.medlem,
    medlemsnummer: p.medlem === "ja" ? (p.medlemsnummer || "") : "",
    kommentar: p.kommentar || "",
    samtykke: p.samtykke === true
  };
}

//DU ER PÅMELDT
//Kjører bare på pameldt.html. Viser svaret fra Java.
const pameldt = document.getElementById("pameldt");

if (pameldt) {
  const pamelding = hentPamelding();
  const kvittering = pamelding && pamelding.bekreftet ? pamelding.kvittering : null;

  if (kvittering) {
    //Vis e-posten personen meldte seg på med (textContent, ikke innerHTML, fordi brukeren har skrevet den)
    document.getElementById("pameldt-epost").textContent = pamelding.epost;
    fyllKvittering(kvittering);

    document.getElementById("pameldt-melding").textContent =
      "Påmeldingen til " + kvittering.kursTittel + " er registrert (referanse " + kvittering.referanse + ")";

    //Venteliste: personen har ikke plass ennå, og det må stå tydelig
    if (kvittering.status === "venteliste") {
      document.getElementById("pameldt-tittel").textContent = "Du står på venteliste";
      document.getElementById("pameldt-melding").textContent =
        "Kurset " + kvittering.kursTittel + " er fullt. Du står på ventelisten (referanse " + kvittering.referanse +
        "). Når noen melder seg av, får førstemann på ventelisten plassen og beskjed på e-post. Du er registrert";
      document.title = "Du står på venteliste – Østfold Husflidslag";
    }

    //Personopplysningene trengs ikke lenger i nettleseren
    lagrePamelding({ bekreftet: true, epost: pamelding.epost, kvittering: kvittering });
  } else {
    //Ingen bekreftet påmelding: vis melding i stedet
    pameldt.hidden = true;
    document.getElementById("ingen-pamelding").hidden = false;
  }
}

//Fyller kursfeltene på pameldt.html med opplysningene fra kvitteringen
function fyllKvittering(kvittering) {
  const verdier = {
    tittel: kvittering.kursTittel,
    dato: new Date(kvittering.dato + "T12:00").toLocaleDateString("nb-NO", { day: "numeric", month: "long", year: "numeric" }),
    tid: kvittering.tidspunkt,
    sted: kvittering.sted,
    lokallag: kortnavn(kvittering.lokallag),
    lagnavn: kortnavn(kvittering.lokallag) + " Husflidslag"
  };
  document.querySelectorAll("[data-kurs]").forEach(function (felt) {
    if (verdier[felt.dataset.kurs] !== undefined) felt.textContent = verdier[felt.dataset.kurs];
    if (felt.dataset.kurs === "dato") felt.dateTime = kvittering.dato;
  });
}

//NYHETSARTIKKEL
//Kjører bare på nyhet.html
//Nyhetene ligger her til backend er på plass. Nyhet 1 står også i HTML-en.
const artikkel = document.getElementById("artikkel");

if (artikkel) {
  const nyheter = [
    {
      id: 1,
      dato: "2024-09-02",
      datoTekst: "2. september 2024",
      lokallag: "Fredrikstad Husflidslag",
      tittel: "Nytt bunadskurs i Fredrikstad – meld deg på nå!",
      ingress: "Vi åpner nå påmelding til høstens bunadskurs i Fredrikstad. Det er kun 8 plasser, og de fylles raskt.",
      tekst: [
        "Vi er glade for å melde at Fredrikstad Husflidslag setter opp nytt bunadskurs til høsten. Kurset starter 20. september og går over 8 tirsdagskvelder.",
        "Kursleder er den erfarne Marit Andersen, som har over 20 år med bunadssøm bak seg. Hun tar imot alle nivåer og har stor tålmodighet med nybegynnere.",
        "Dette er en ypperlig mulighet til å lære en gammel håndverkstradisjon i et hyggelig miljø. Mange av deltakerne fra tidligere kurs fremhever de sosiale sidene som noe av det beste med å delta.",
        "Meld deg på så snart som mulig – det er allerede 5 av 8 plasser tatt."
      ]
    },
    {
      id: 2,
      dato: "2024-09-05",
      datoTekst: "5. september 2024",
      lokallag: "Moss Husflidslag",
      tittel: "Ledige plasser på vevkurs i Moss",
      ingress: "Det er fortsatt ledige plasser på høstens vevkurs i Moss. Et flott kurs for deg som vil lære tradisjonell båndbinding.",
      tekst: [
        "Moss Husflidslag arrangerer grunnkurs i båndveving 5. og 6. oktober. Kurset passer for nybegynnere, og alt utstyr finnes i vevstudioet vårt.",
        "Du lærer å sette opp en båndvev, velge garn og veve enkle mønstre. Etter to dager har du et ferdig bånd med deg hjem.",
        "Det er 6 ledige plasser igjen. Meld deg på via kurssiden."
      ]
    },
    {
      id: 3,
      dato: "2024-08-28",
      datoTekst: "28. august 2024",
      lokallag: "Østfold Husflidslag",
      tittel: "Høstprogrammet er klart – se alle kurs og aktiviteter",
      ingress: "Vi har satt opp et rikt høstprogram med kurs innen strikking, veving, søm, broderi, bunad og trearbeid.",
      tekst: [
        "Høstens program er nå publisert, og lokallagene i hele Østfold har satt opp kurs for både nybegynnere og erfarne håndverkere.",
        "Nytt i år er flere helgekurs og et eget kurs i tradisjonell treskjæring i Fredrikstad.",
        "Du kan delta på kurs i alle lokallag, uansett hvor du bor. Se hele oversikten under Kurs og aktiviteter."
      ]
    },
    {
      id: 4,
      dato: "2024-08-20",
      datoTekst: "20. august 2024",
      lokallag: "Fredrikstad Husflidslag",
      tittel: "Velkommen til medlemskveld i september",
      ingress: "Alle medlemmer inviteres til en hyggelig sammenkomst hos Fredrikstad Husflidslag 14. september.",
      tekst: [
        "Vi samles i lokalene våre i Nygårdsgata 14 lørdag 14. september klokken 18.",
        "Det blir kaffe og kake, en kort gjennomgang av høstens kurs og god tid til å vise frem det du har laget.",
        "Ta gjerne med en venn som er nysgjerrig på husflid. Alle er velkomne."
      ]
    }
  ];

  //Finn nyheten fra adressen (f.eks. nyhet.html?id=2)
  const id = Number(new URLSearchParams(window.location.search).get("id")) || 1;
  const nyhet = nyheter.find(function (n) { return n.id === id; });

  if (nyhet) {
    document.title = nyhet.tittel + " – Østfold Husflidslag";
    document.getElementById("artikkel-tittel").textContent = nyhet.tittel;
    document.getElementById("artikkel-ingress").textContent = nyhet.ingress;
    document.getElementById("artikkel-lokallag").textContent = nyhet.lokallag;

    const dato = document.getElementById("artikkel-dato");
    dato.textContent = nyhet.datoTekst;
    dato.setAttribute("datetime", nyhet.dato);

    const brodsmule = document.getElementById("brodsmule-tittel");
    brodsmule.textContent = nyhet.tittel;
    brodsmule.href = "nyhet.html?id=" + nyhet.id;

    //Bytt ut avsnittene
    const tekst = document.getElementById("artikkel-tekst");
    tekst.replaceChildren();
    nyhet.tekst.forEach(function (avsnitt) {
      const p = document.createElement("p");
      p.textContent = avsnitt;
      tekst.append(p);
    });
  }

  //Del på Facebook: lenken får adressen til denne siden
  const delFacebook = document.getElementById("del-facebook");
  delFacebook.href = "https://www.facebook.com/sharer/sharer.php?u=" + encodeURIComponent(window.location.href);

  //Del på Instagram: Instagram har ingen del-lenke, så vi kopierer adressen
  const delInstagram = document.getElementById("del-instagram");
  const delMelding = document.getElementById("del-melding");

  delInstagram.addEventListener("click", function () {
    navigator.clipboard.writeText(window.location.href).then(function () {
      delMelding.textContent = "Lenken er kopiert. Lim den inn i et innlegg eller en historie på Instagram.";
    }).catch(function () {
      delMelding.textContent = "Kopier adressen fra adressefeltet og lim den inn på Instagram.";
    });
    delMelding.hidden = false;
  });
}

//BLI MEDLEM-SKJEMA
//Kjører bare på bli-medlem.html
const medlemSkjema = document.getElementById("medlem-skjema");

if (medlemSkjema) {
  const medlemFeil = document.getElementById("medlem-feil");
  const medlemTakk = document.getElementById("medlem-takk");

  //Fjern rød ramme når feltet blir riktig
  medlemSkjema.addEventListener("input", function (hendelse) {
    if (hendelse.target.checkValidity()) {
      hendelse.target.removeAttribute("aria-invalid");
    }
  });

  //Sjekk skjemaet og vis takk
  medlemSkjema.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();

    const ugyldige = Array.from(medlemSkjema.elements).filter(function (felt) {
      return felt.willValidate && !felt.checkValidity();
    });

    medlemSkjema.querySelectorAll("[aria-invalid]").forEach(function (felt) {
      felt.removeAttribute("aria-invalid");
    });

    if (ugyldige.length > 0) {
      ugyldige.forEach(function (felt) {
        felt.setAttribute("aria-invalid", "true");
      });
      medlemFeil.textContent = "Fyll ut alle feltene, velg lokallag og kryss av for samtykke.";
      medlemFeil.hidden = false;
      ugyldige[0].focus();
      return;
    }

    //Sendes ikke noe sted ennå. Det kommer med backend.
    const navn = document.getElementById("medlem-navn").value.trim();
    const lokallag = document.getElementById("medlem-lokallag");
    const lagNavn = lokallag.options[lokallag.selectedIndex].text;

    medlemTakk.textContent = "Takk, " + navn + "! " + lagNavn + " Husflidslag tar kontakt med deg om medlemskapet.";
    medlemSkjema.hidden = true;
    medlemTakk.hidden = false;
    medlemTakk.focus();
  });
}

//LAGRE PÅMELDINGEN MELLOM SIDENE
//sessionStorage husker data til fanen lukkes. Erstattes av backend senere.
function lagrePamelding(data) {
  try {
    sessionStorage.setItem("pamelding", JSON.stringify(data));
  } catch (feil) {
    //Nettleseren tillater ikke lagring, f.eks. i privat modus
  }
}

function hentPamelding() {
  try {
    return JSON.parse(sessionStorage.getItem("pamelding"));
  } catch (feil) {
    return null;
  }
}

function slettPamelding() {
  try {
    sessionStorage.removeItem("pamelding");
  } catch (feil) {
    //Ingenting å slette
  }
}

//HENTE DATA FRA JAVA
//Felles hjelpefunksjon: henter JSON fra en adresse i API-et
async function hentJson(adresse) {
  const svar = await fetch(adresse);
  if (!svar.ok) {
    throw new Error("Feil fra serveren: " + svar.status);
  }
  return svar.json();
}

//"Fredrikstad Husflidslag" → "Fredrikstad"
function kortnavn(navn) {
  return (navn || "").replace(" Husflidslag", "");
}

//Gjør et kurs fra Java om til formatet fyllKurs() forventer
function tilKursVisning(k) {
  return {
    id: k.id,
    tittel: k.tittel,
    tema: k.tema,
    lokallag: kortnavn(k.lokallag),
    dato: k.dato,
    tid: k.tidspunkt,
    varighet: k.varighet,
    sted: k.sted,
    kursleder: "",
    malgruppe: k.malgruppe,
    niva: k.niva,
    plasser: k.kapasitet,
    ledige: k.ledigePlasser,
    medlemspris: Number(k.medlemspris),
    ordinaerpris: Number(k.ordinaerpris),
    beskrivelse: k.beskrivelse,
    utstyr: k.utstyr,
    kontakt: k.kontakt,
    bildesti: k.bildesti,
    bildealt: k.bildealt
  };
}

//Henter ett kurs fra Java. Gir null hvis kurset ikke finnes.
async function hentKursFraApi(id) {
  if (!Number.isInteger(id) || id <= 0) return null;
  try {
    return tilKursVisning(await hentJson("/api/kurs/" + id));
  } catch (feil) {
    console.error(feil);
    return null;
  }
}

