"use strict";

//ADMINISTRASJON
//Brukes av admin.html, admin-kurs.html, admin-nyheter.html og admin-pameldinger.html.
//Dette er en demo uten server: data lagres bare i nettleseren (sessionStorage)
//og innloggingen sjekker ikke passordet. Begge deler må flyttes til serveren.

//DEMO-DATA
//Samme kurs og nyheter som på den offentlige nettsiden
const DEMO_KURS = [
  { id: 1, tittel: "Bunadssøm for nybegynnere", dato: "2024-09-20", lokallag: "Fredrikstad", plasser: 8 },
  { id: 2, tittel: "Strikk din egen genser", dato: "2024-09-27", lokallag: "Sarpsborg", plasser: 4 },
  { id: 3, tittel: "Vevkurs – grunnleggende båndbinding", dato: "2024-10-05", lokallag: "Moss", plasser: 10 }
];

const DEMO_NYHETER = [
  { id: 1, tittel: "Nytt bunadskurs i Fredrikstad – meld deg på nå!", dato: "2024-09-02", lokallag: "Fredrikstad",
    ingress: "Vi åpner nå påmelding til høstens bunadskurs i Fredrikstad. Det er kun 8 plasser, og de fylles raskt." },
  { id: 2, tittel: "Ledige plasser på vevkurs i Moss", dato: "2024-09-05", lokallag: "Moss",
    ingress: "Det er fortsatt ledige plasser på høstens vevkurs i Moss." },
  { id: 3, tittel: "Høstprogrammet er klart – se alle kurs og aktiviteter", dato: "2024-08-28", lokallag: "Østfold",
    ingress: "Vi har satt opp et rikt høstprogram med kurs innen strikking, veving, søm, broderi, bunad og trearbeid." }
];

const DEMO_PAMELDINGER = [
  { id: 1, kursId: 1, navn: "Kari Nordmann", epost: "kari@eksempel.no", telefon: "900 11 222", medlem: true, status: "pameldt" },
  { id: 2, kursId: 1, navn: "Ingrid Olsen", epost: "ingrid@eksempel.no", telefon: "911 22 333", medlem: true, status: "pameldt" },
  { id: 3, kursId: 1, navn: "Astrid Berg", epost: "astrid@eksempel.no", telefon: "922 33 444", medlem: false, status: "pameldt" },
  { id: 4, kursId: 1, navn: "Liv Johansen", epost: "liv@eksempel.no", telefon: "933 44 555", medlem: true, status: "pameldt" },
  { id: 5, kursId: 1, navn: "Solveig Hansen", epost: "solveig@eksempel.no", telefon: "944 55 666", medlem: false, status: "pameldt" },
  { id: 6, kursId: 2, navn: "Marit Larsen", epost: "marit@eksempel.no", telefon: "955 66 777", medlem: true, status: "pameldt" },
  { id: 7, kursId: 2, navn: "Berit Pedersen", epost: "berit@eksempel.no", telefon: "966 77 888", medlem: true, status: "pameldt" },
  { id: 8, kursId: 2, navn: "Turid Andersen", epost: "turid@eksempel.no", telefon: "977 88 999", medlem: false, status: "pameldt" },
  { id: 9, kursId: 2, navn: "Randi Nilsen", epost: "randi@eksempel.no", telefon: "988 99 000", medlem: true, status: "pameldt" },
  { id: 10, kursId: 2, navn: "Gunn Kristiansen", epost: "gunn@eksempel.no", telefon: "412 34 567", medlem: false, status: "venteliste" },
  { id: 11, kursId: 2, navn: "Hilde Jensen", epost: "hilde@eksempel.no", telefon: "423 45 678", medlem: true, status: "venteliste" },
  { id: 12, kursId: 3, navn: "Anne Karlsen", epost: "anne@eksempel.no", telefon: "434 56 789", medlem: true, status: "pameldt" },
  { id: 13, kursId: 3, navn: "Eva Halvorsen", epost: "eva@eksempel.no", telefon: "445 67 890", medlem: false, status: "pameldt" },
  { id: 14, kursId: 3, navn: "Tone Eriksen", epost: "tone@eksempel.no", telefon: "456 78 901", medlem: true, status: "pameldt" },
  { id: 15, kursId: 3, navn: "Wenche Moen", epost: "wenche@eksempel.no", telefon: "467 89 012", medlem: true, status: "pameldt" }
];

//LAGRING I NETTLESEREN
//Endringer huskes til fanen lukkes. Erstattes av backend senere.
function hentListe(navn, demo) {
  try {
    const lagret = JSON.parse(sessionStorage.getItem("admin-" + navn));
    if (Array.isArray(lagret)) return lagret;
  } catch (feil) {
    //Ugyldig eller blokkert lagring, bruker demo-data
  }
  return demo.map((rad) => ({ ...rad }));
}

function lagreListe(navn, liste) {
  try {
    sessionStorage.setItem("admin-" + navn, JSON.stringify(liste));
  } catch (feil) {
    //Nettleseren tillater ikke lagring, f.eks. i privat modus
  }
}

function erInnlogget() {
  try {
    return sessionStorage.getItem("admin-innlogget") === "ja";
  } catch (feil) {
    return false;
  }
}

function settInnlogget(verdi) {
  try {
    if (verdi) sessionStorage.setItem("admin-innlogget", "ja");
    else sessionStorage.removeItem("admin-innlogget");
  } catch (feil) {
    //Ingenting å lagre
  }
}

function nesteId(liste) {
  return liste.reduce((storst, rad) => Math.max(storst, rad.id), 0) + 1;
}

//HJELPEFUNKSJONER
//Lager <time> med norsk dato, f.eks. 20. september 2024
function lagDato(iso) {
  const time = document.createElement("time");
  time.dateTime = iso;
  time.textContent = new Date(iso + "T12:00").toLocaleDateString("nb-NO", {
    day: "numeric", month: "long", year: "numeric"
  });
  return time;
}

function lagCelle(innhold, tag = "td") {
  const celle = document.createElement(tag);
  if (innhold instanceof Node) celle.append(innhold);
  else celle.textContent = innhold;
  return celle;
}

function lagKnapp(tekst, skjultTekst, klasse) {
  const knapp = document.createElement("button");
  knapp.type = "button";
  knapp.className = "admin-knapp " + (klasse || "");
  knapp.textContent = tekst;
  const skjult = document.createElement("span");
  skjult.className = "skjult";
  skjult.textContent = ": " + skjultTekst;
  knapp.append(skjult);
  return knapp;
}

//Sletting krever to klikk, så man ikke sletter ved et uhell
function lagSlettKnapp(navn, vedSletting) {
  const knapp = lagKnapp("Slett", navn, "admin-knapp-slett");
  knapp.addEventListener("click", () => {
    if (knapp.dataset.bekreft === "ja") {
      vedSletting();
      return;
    }
    knapp.dataset.bekreft = "ja";
    knapp.firstChild.textContent = "Bekreft sletting";
  });
  knapp.addEventListener("blur", () => {
    delete knapp.dataset.bekreft;
    knapp.firstChild.textContent = "Slett";
  });
  return knapp;
}

function lagHandlinger(...knapper) {
  const celle = document.createElement("td");
  celle.className = "admin-handlinger";
  celle.append(...knapper);
  return celle;
}

//DIALOG MED SKJEMA
//Åpner dialogen og fyller inn feltene når man redigerer
function apneDialog(dialog, tittel, data) {
  const skjema = dialog.querySelector("form");
  skjema.reset();
  skjema.querySelectorAll("[aria-invalid]").forEach((felt) => felt.removeAttribute("aria-invalid"));
  skjema.querySelector(".skjema-feil").hidden = true;
  dialog.querySelector("h2").textContent = tittel;

  Object.entries(data || {}).forEach(([navn, verdi]) => {
    const felt = skjema.elements[navn];
    if (felt) felt.value = verdi;
  });

  dialog.showModal();
}

//Sjekker skjemaet og gir dataene videre hvis alt er fylt ut riktig
function kobleDialog(dialog, vedLagring) {
  const skjema = dialog.querySelector("form");
  const feil = skjema.querySelector(".skjema-feil");

  dialog.querySelector(".lukk-dialog").addEventListener("click", () => dialog.close());

  skjema.addEventListener("submit", (hendelse) => {
    hendelse.preventDefault();

    let forsteFeil = null;
    skjema.querySelectorAll("input:not([type=hidden]), select, textarea").forEach((felt) => {
      const gyldig = felt.checkValidity();
      felt.setAttribute("aria-invalid", String(!gyldig));
      if (!gyldig && !forsteFeil) forsteFeil = felt;
    });

    if (forsteFeil) {
      feil.textContent = "Fyll ut alle feltene riktig før du lagrer.";
      feil.hidden = false;
      forsteFeil.focus();
      return;
    }

    vedLagring(Object.fromEntries(new FormData(skjema)));
    dialog.close();
  });
}

//INNLOGGING
//Demo: sjekker bare at feltene er fylt ut. Ekte innlogging må gjøres på serveren.
const innloggingSkjema = document.getElementById("innlogging-skjema");

if (innloggingSkjema) {
  if (erInnlogget()) location.replace("admin-kurs.html");

  const innloggingFeil = document.getElementById("innlogging-feil");

  innloggingSkjema.addEventListener("submit", (hendelse) => {
    hendelse.preventDefault();

    const felter = [...innloggingSkjema.querySelectorAll("input")];
    felter.forEach((felt) => felt.setAttribute("aria-invalid", String(!felt.checkValidity())));
    const forsteFeil = felter.find((felt) => !felt.checkValidity());

    if (forsteFeil) {
      innloggingFeil.textContent = "Skriv inn gyldig e-post og passord.";
      innloggingFeil.hidden = false;
      forsteFeil.focus();
      return;
    }

    settInnlogget(true);
    location.href = "admin-kurs.html";
  });
}

//SJEKK INNLOGGING PÅ ADMINSIDENE
const adminSide = document.body.dataset.adminSide;

if (adminSide) {
  if (!erInnlogget()) {
    location.replace("admin.html");
  } else {
    document.body.classList.add("innlogget");
  }
}

//LOGG UT
document.querySelectorAll(".logg-ut").forEach((knapp) => {
  knapp.addEventListener("click", () => {
    settInnlogget(false);
    location.href = "admin.html";
  });
});

//KURS
if (adminSide === "kurs") {
  let kurs = hentListe("kurs", DEMO_KURS);
  const pameldinger = hentListe("pameldinger", DEMO_PAMELDINGER);
  const rader = document.getElementById("kurs-rader");
  const tom = document.getElementById("kurs-tom");
  const dialog = document.getElementById("kurs-dialog");

  const antallPameldt = (id) =>
    pameldinger.filter((p) => p.kursId === id && p.status === "pameldt").length;

  function visKurs() {
    rader.replaceChildren();
    [...kurs].sort((a, b) => a.dato.localeCompare(b.dato)).forEach((k) => {
      const rad = document.createElement("tr");
      const pameldt = antallPameldt(k.id);

      const plasser = document.createElement("span");
      plasser.textContent = pameldt + " av " + k.plasser;
      if (pameldt >= k.plasser) plasser.className = "admin-fullt";

      const rediger = lagKnapp("Rediger", k.tittel);
      rediger.addEventListener("click", () => apneDialog(dialog, "Rediger kurs", k));

      const slett = lagSlettKnapp(k.tittel, () => {
        kurs = kurs.filter((rad) => rad.id !== k.id);
        lagreListe("kurs", kurs);
        lagreListe("pameldinger", pameldinger.filter((p) => p.kursId !== k.id));
        visKurs();
      });

      rad.append(lagCelle(k.tittel, "th"), lagCelle(lagDato(k.dato)), lagCelle(k.lokallag), lagCelle(plasser), lagHandlinger(rediger, slett));
      rad.firstChild.scope = "row";
      rader.append(rad);
    });

    tom.hidden = kurs.length > 0;

    //Tallene øverst
    const ider = kurs.map((k) => k.id);
    document.getElementById("tall-kurs").textContent = kurs.length;
    document.getElementById("tall-pameldte").textContent =
      pameldinger.filter((p) => ider.includes(p.kursId) && p.status === "pameldt").length;
    document.getElementById("tall-venteliste").textContent =
      pameldinger.filter((p) => ider.includes(p.kursId) && p.status === "venteliste").length;
  }

  document.getElementById("nytt-kurs").addEventListener("click", () => apneDialog(dialog, "Nytt kurs"));

  kobleDialog(dialog, (data) => {
    const id = Number(data.id);
    const nytt = { tittel: data.tittel.trim(), dato: data.dato, lokallag: data.lokallag, plasser: Number(data.plasser) };

    if (id) {
      kurs = kurs.map((k) => (k.id === id ? { ...k, ...nytt } : k));
    } else {
      kurs.push({ id: nesteId(kurs), ...nytt });
    }
    lagreListe("kurs", kurs);
    visKurs();
  });

  visKurs();
}

//NYHETER
if (adminSide === "nyheter") {
  let nyheter = hentListe("nyheter", DEMO_NYHETER);
  const rader = document.getElementById("nyhet-rader");
  const tom = document.getElementById("nyhet-tom");
  const dialog = document.getElementById("nyhet-dialog");

  function visNyheter() {
    rader.replaceChildren();
    //Nyeste først
    [...nyheter].sort((a, b) => b.dato.localeCompare(a.dato)).forEach((n) => {
      const rad = document.createElement("tr");

      const rediger = lagKnapp("Rediger", n.tittel);
      rediger.addEventListener("click", () => apneDialog(dialog, "Rediger nyhet", n));

      const slett = lagSlettKnapp(n.tittel, () => {
        nyheter = nyheter.filter((rad) => rad.id !== n.id);
        lagreListe("nyheter", nyheter);
        visNyheter();
      });

      rad.append(lagCelle(n.tittel, "th"), lagCelle(lagDato(n.dato)), lagCelle(n.lokallag), lagHandlinger(rediger, slett));
      rad.firstChild.scope = "row";
      rader.append(rad);
    });

    tom.hidden = nyheter.length > 0;
  }

  document.getElementById("ny-nyhet").addEventListener("click", () =>
    apneDialog(dialog, "Ny nyhet", { dato: new Date().toISOString().slice(0, 10) })
  );

  kobleDialog(dialog, (data) => {
    const id = Number(data.id);
    const ny = { tittel: data.tittel.trim(), dato: data.dato, lokallag: data.lokallag, ingress: data.ingress.trim() };

    if (id) {
      nyheter = nyheter.map((n) => (n.id === id ? { ...n, ...ny } : n));
    } else {
      nyheter.push({ id: nesteId(nyheter), ...ny });
    }
    lagreListe("nyheter", nyheter);
    visNyheter();
  });

  visNyheter();
}

//PÅMELDINGER
if (adminSide === "pameldinger") {
  const kurs = hentListe("kurs", DEMO_KURS);
  let pameldinger = hentListe("pameldinger", DEMO_PAMELDINGER);
  const rader = document.getElementById("pamelding-rader");
  const tom = document.getElementById("pamelding-tom");
  const antall = document.getElementById("pamelding-antall");
  const filter = document.getElementById("filter-kurs");

  //Kursene i nedtrekkslisten
  kurs.forEach((k) => filter.add(new Option(k.tittel, k.id)));

  //Kan velge kurs i adressen, f.eks. admin-pameldinger.html?kurs=2
  const valgtKurs = new URLSearchParams(location.search).get("kurs");
  if (valgtKurs) filter.value = valgtKurs;

  const kursNavn = (id) => (kurs.find((k) => k.id === id) || {}).tittel || "Slettet kurs";

  function visPameldinger() {
    rader.replaceChildren();
    const kursId = Number(filter.value);
    const synlige = pameldinger
      .filter((p) => !kursId || p.kursId === kursId)
      //Påmeldte før venteliste
      .sort((a, b) => a.status.localeCompare(b.status) || a.navn.localeCompare(b.navn));

    synlige.forEach((p) => {
      const rad = document.createElement("tr");

      //E-post og telefon som lenker
      const kontakt = document.createElement("td");
      const epost = document.createElement("a");
      epost.href = "mailto:" + p.epost;
      epost.textContent = p.epost;
      const telefon = document.createElement("a");
      telefon.href = "tel:+47" + p.telefon.replace(/\s/g, "");
      telefon.textContent = p.telefon;
      kontakt.className = "admin-kontakt";
      kontakt.append(epost, telefon);

      const status = document.createElement("span");
      status.className = "plasser " + (p.status === "venteliste" ? "plasser-faa" : "plasser-ledig");
      status.textContent = p.status === "venteliste" ? "Venteliste" : "Påmeldt";

      const knapper = [];
      if (p.status === "venteliste") {
        const giPlass = lagKnapp("Gi plass", p.navn);
        giPlass.addEventListener("click", () => {
          pameldinger = pameldinger.map((rad) => (rad.id === p.id ? { ...rad, status: "pameldt" } : rad));
          lagreListe("pameldinger", pameldinger);
          visPameldinger();
        });
        knapper.push(giPlass);
      }
      knapper.push(lagSlettKnapp(p.navn, () => {
        pameldinger = pameldinger.filter((rad) => rad.id !== p.id);
        lagreListe("pameldinger", pameldinger);
        visPameldinger();
      }));

      rad.append(lagCelle(p.navn, "th"), kontakt, lagCelle(kursNavn(p.kursId)), lagCelle(p.medlem ? "Ja" : "Nei"), lagCelle(status), lagHandlinger(...knapper));
      rad.firstChild.scope = "row";
      rader.append(rad);
    });

    antall.textContent = "(" + synlige.length + ")";
    tom.hidden = synlige.length > 0;
  }

  filter.addEventListener("change", visPameldinger);
  visPameldinger();
}
