"use strict";

const DEMO_KURS = [
  { id: 1, tittel: "Bunadssøm for nybegynnere", tema: "Bunad", lokallag: "Fredrikstad", status: "PUBLISERT",
    beskrivelse: "Lær å sy din egen bunad fra bunnen av. Kurset passer deg som aldri har sydd bunad før.",
    dato: "2026-11-20", starttid: "18:00", sluttid: "21:00", varighet: "8 kvelder",
    sted: "Fredrikstad Husflidssenter, Nygårdsgt. 14", kursleder: "Marit Andersen", malgruppe: "Voksne", niva: "Nybegynner",
    utstyr: "Symaskin hvis du har, stoff og tråd", medlemspris: 1500, ordinaerpris: 2000, plasser: 8,
    kontaktperson: "Kari Hansen", bilde: "../bilder/kurs-1.jpg", bildeAlt: "Broderi med trådsnelle" },
  { id: 2, tittel: "Strikk din egen genser", tema: "Strikking", lokallag: "Sarpsborg", status: "PUBLISERT",
    beskrivelse: "Strikk en klassisk genser i ditt eget tempo, med hjelp fra erfarne strikkere.",
    dato: "2026-11-27", starttid: "10:00", sluttid: "14:00", varighet: "6 lørdager",
    sted: "Sarpsborg Husflidslag", kursleder: "Anne Berg", malgruppe: "Voksne", niva: "Litt erfaren",
    utstyr: "Strikkepinner nr. 4", medlemspris: 1200, ordinaerpris: 1600, plasser: 4,
    kontaktperson: "Kari Hansen", bilde: "../bilder/kurs-2.jpg", bildeAlt: "Gammel vevstol med tråder" },
  { id: 3, tittel: "Vevkurs – grunnleggende båndbinding", tema: "Veving", lokallag: "Moss", status: "PUBLISERT",
    beskrivelse: "Grunnkurs i båndveving for nybegynnere. Alt utstyr finnes i vevstudioet.",
    dato: "2026-12-05", starttid: "10:00", sluttid: "15:00", varighet: "2 dager",
    sted: "Moss Husflidslag, vevstudioet", kursleder: "Liv Johansen", malgruppe: "Alle", niva: "Nybegynner",
    utstyr: "Alt utstyr finnes på stedet", medlemspris: 900, ordinaerpris: 1200, plasser: 10,
    kontaktperson: "Silje Ole", bilde: "../bilder/kurs-3.jpg", bildeAlt: "Fargerik vevstol" }
];

const DEMO_NYHETER = [
  { id: 1, tittel: "Nytt bunadskurs i Fredrikstad – meld deg på nå!", dato: "2026-11-02", lokallag: "Fredrikstad",
    ingress: "Vi åpner nå påmelding til bunadskurset i Fredrikstad. Det er kun 8 plasser, og de fylles raskt.",
    tekst: "Fredrikstad Husflidslag setter opp nytt bunadskurs i november. Kurset starter 20. november og går over 8 kvelder.",
    bilde: "../bilder/nyhet-1.jpg", bildeAlt: "Kvinne i bunad som hjelper med broderi" },
  { id: 2, tittel: "Ledige plasser på vevkurs i Moss", dato: "2026-11-05", lokallag: "Moss",
    ingress: "Det er fortsatt ledige plasser på vevkurset i Moss.",
    tekst: "Moss Husflidslag arrangerer grunnkurs i båndveving 5. og 6. desember. Kurset passer for nybegynnere.",
    bilde: "../bilder/nyhet-2.jpg", bildeAlt: "Plakat for Husflidsdagen" },
  { id: 3, tittel: "Høstprogrammet er klart – se alle kurs og aktiviteter", dato: "2026-10-28", lokallag: "Østfold",
    ingress: "Vi har satt opp et rikt program med kurs innen strikking, veving, søm, broderi, bunad og trearbeid.",
    tekst: "Se alle kursene under Kurs og aktiviteter. Medlemmer får rabatt på alle kurs.",
    bilde: "../bilder/nyhet-3.jpg", bildeAlt: "Gruppe som jobber sammen i et verksted" }
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

function hentListe(navn, demo) {
  try {
    const lagret = JSON.parse(sessionStorage.getItem("admin-" + navn));
    if (Array.isArray(lagret)) return lagret;
  } catch (feil) {
  }
  return demo.map((rad) => ({ ...rad }));
}

function lagreListe(navn, liste) {
  try {
    sessionStorage.setItem("admin-" + navn, JSON.stringify(liste));
    return true;
  } catch (feil) {
    return false;
  }
}

async function hentInnloggetBruker() {
  try {
    const svar = await fetch("/api/meg");
    if (!svar.ok) return null;
    return await svar.json();
  } catch (feil) {
    return null;
  }
}

function nesteId(liste) {
  return liste.reduce((storst, rad) => Math.max(storst, rad.id), 0) + 1;
}

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

function apneDialog(dialog, tittel, data) {
  const skjema = dialog.querySelector("form");
  skjema.reset();
  skjema.querySelectorAll("[aria-invalid]").forEach((felt) => felt.removeAttribute("aria-invalid"));
  skjema.querySelector(".skjema-feil").hidden = true;
  dialog.querySelector("h2").textContent = tittel;

  Object.entries(data || {}).forEach(([navn, verdi]) => {
    const felt = skjema.elements[navn];
    if (felt && felt.type !== "file") felt.value = verdi;
  });

  visBilde(dialog);
  dialog.showModal();
}

function kobleDialog(dialog, vedLagring) {
  const skjema = dialog.querySelector("form");
  const feil = skjema.querySelector(".skjema-feil");

  dialog.querySelector(".lukk-dialog").addEventListener("click", () => dialog.close());

  skjema.addEventListener("input", (hendelse) => {
    if (hendelse.target.checkValidity()) hendelse.target.removeAttribute("aria-invalid");
  });
  skjema.addEventListener("change", (hendelse) => {
    if (hendelse.target.checkValidity()) hendelse.target.removeAttribute("aria-invalid");
  });

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

    const melding = vedLagring(Object.fromEntries(new FormData(skjema)));
    if (melding) {
      feil.textContent = melding;
      feil.hidden = false;
      return;
    }
    dialog.close();
  });
}

const MAKS_BILDE_MB = 10;

function visBilde(dialog) {
  const skjult = dialog.querySelector('input[name="bilde"]');
  if (!skjult) return;
  const figur = dialog.querySelector(".bilde-forhandsvisning");
  const alt = dialog.querySelector('input[name="bildeAlt"]');
  figur.hidden = !skjult.value;
  figur.querySelector("img").src = skjult.value || "";
  alt.required = Boolean(skjult.value);
}

function kobleBilde(dialog) {
  const fil = dialog.querySelector('input[type="file"]');
  const skjult = dialog.querySelector('input[name="bilde"]');
  const feil = dialog.querySelector(".skjema-feil");

  fil.addEventListener("change", () => {
    const valgt = fil.files[0];
    if (!valgt) return;

    if (!["image/jpeg", "image/png", "image/webp"].includes(valgt.type)) {
      feil.textContent = "Filen må være et bilde (JPG, PNG eller WebP).";
      feil.hidden = false;
      fil.value = "";
      return;
    }
    if (valgt.size > MAKS_BILDE_MB * 1024 * 1024) {
      feil.textContent = "Bildet er for stort. Det kan være maks " + MAKS_BILDE_MB + " MB.";
      feil.hidden = false;
      fil.value = "";
      return;
    }

    const bilde = new Image();
    bilde.onload = () => {
      const skala = Math.min(1, 1200 / bilde.width);
      const lerret = document.createElement("canvas");
      lerret.width = Math.round(bilde.width * skala);
      lerret.height = Math.round(bilde.height * skala);
      lerret.getContext("2d").drawImage(bilde, 0, 0, lerret.width, lerret.height);
      skjult.value = lerret.toDataURL("image/jpeg", 0.82);
      URL.revokeObjectURL(bilde.src);
      feil.hidden = true;
      visBilde(dialog);
    };
    bilde.src = URL.createObjectURL(valgt);
  });

  dialog.querySelector(".fjern-bilde").addEventListener("click", () => {
    skjult.value = "";
    fil.value = "";
    visBilde(dialog);
  });
}

function lagMiniBilde(rad) {
  if (!rad.bilde) return "–";
  const img = document.createElement("img");
  img.src = rad.bilde;
  img.alt = "";
  img.className = "admin-mini-bilde";
  return img;
}

const innloggingSkjema = document.getElementById("innlogging-skjema");

if (innloggingSkjema) {
  const demoInfo = document.querySelector(".demo-info");
  if (demoInfo) demoInfo.remove();

  hentInnloggetBruker().then((bruker) => {
    if (bruker) location.replace("admin-kurs.html");
  });

  const innloggingFeil = document.getElementById("innlogging-feil");
  const knapp = innloggingSkjema.querySelector('button[type="submit"]');

  function visFeil(tekst) {
    innloggingFeil.textContent = tekst;
    innloggingFeil.hidden = false;
  }

  innloggingSkjema.addEventListener("submit", async (hendelse) => {
    hendelse.preventDefault();

    const felter = [...innloggingSkjema.querySelectorAll("input")];
    felter.forEach((felt) => felt.setAttribute("aria-invalid", String(!felt.checkValidity())));
    const forsteFeil = felter.find((felt) => !felt.checkValidity());

    if (forsteFeil) {
      visFeil("Skriv inn gyldig e-post og passord.");
      forsteFeil.focus();
      return;
    }

    const epost = innloggingSkjema.elements.epost.value.trim().toLowerCase();
    const passord = innloggingSkjema.elements.passord.value;

    if (knapp) knapp.disabled = true;
    try {
      const svar = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ epost: epost, passord: passord })
      });

      if (svar.ok) {
        location.href = "admin-kurs.html";
        return;
      }

      const innhold = await svar.json().catch(() => ({}));
      visFeil(innhold.melding || "Feil e-post eller passord. Prøv igjen.");
      innloggingSkjema.elements.passord.value = "";
      innloggingSkjema.elements.passord.focus();
    } catch (feil) {
      visFeil("Fikk ikke kontakt med serveren. Prøv igjen.");
    } finally {
      if (knapp) knapp.disabled = false;
    }
  });
}

const adminSide = document.body.dataset.adminSide;
let innloggetBruker = null;

if (adminSide) {
  hentInnloggetBruker().then((bruker) => {
    if (!bruker) {
      location.replace("admin.html");
      return;
    }
    innloggetBruker = bruker;
    document.body.classList.add("innlogget");
  });
}

document.querySelectorAll(".logg-ut").forEach((knapp) => {
  knapp.addEventListener("click", async () => {
    try {
      await fetch("/api/logout", { method: "POST" });
    } catch (feil) {
      console.error(feil);
    }
    location.href = "admin.html";
  });
});

if (adminSide === "kurs") {
  let kurs = hentListe("kurs", DEMO_KURS);
  const pameldinger = hentListe("pameldinger", DEMO_PAMELDINGER);
  const rader = document.getElementById("kurs-rader");
  const tom = document.getElementById("kurs-tom");
  const dialog = document.getElementById("kurs-dialog");
  const skjema = document.getElementById("kurs-skjema");

  const STATUS = { PUBLISERT: "Publisert", KLADD: "Kladd", AVLYST: "Avlyst" };

  const antallPameldt = (id) =>
    pameldinger.filter((p) => p.kursId === id && p.status === "pameldt").length;

  skjema.addEventListener("input", () => {
    const start = skjema.elements.starttid.value;
    const slutt = skjema.elements.sluttid;
    slutt.setCustomValidity(start && slutt.value && slutt.value <= start ? "Sluttid må være etter starttid" : "");
  });

  function visKurs() {
    rader.replaceChildren();
    [...kurs].sort((a, b) => a.dato.localeCompare(b.dato)).forEach((k) => {
      const rad = document.createElement("tr");
      const pameldt = antallPameldt(k.id);

      const tittel = document.createElement("th");
      tittel.scope = "row";
      const tema = document.createElement("span");
      tema.className = "admin-under";
      tema.textContent = k.tema;
      tittel.append(k.tittel, tema);

      const dato = document.createElement("td");
      const tid = document.createElement("span");
      tid.className = "admin-under";
      tid.textContent = k.starttid + "–" + k.sluttid;
      dato.append(lagDato(k.dato), tid);

      const plasser = document.createElement("span");
      plasser.textContent = pameldt + " av " + k.plasser;
      if (pameldt >= k.plasser) plasser.className = "admin-fullt";

      const status = document.createElement("span");
      status.className = "admin-status admin-status-" + k.status.toLowerCase();
      status.textContent = STATUS[k.status] || k.status;

      const rediger = lagKnapp("Rediger", k.tittel);
      rediger.addEventListener("click", () => apneDialog(dialog, "Rediger kurs", k));

      const slett = lagSlettKnapp(k.tittel, () => {
        kurs = kurs.filter((rad) => rad.id !== k.id);
        lagreListe("kurs", kurs);
        lagreListe("pameldinger", pameldinger.filter((p) => p.kursId !== k.id));
        visKurs();
      });

      rad.append(lagCelle(lagMiniBilde(k)), tittel, dato, lagCelle(k.lokallag), lagCelle(plasser), lagCelle(status), lagHandlinger(rediger, slett));
      rader.append(rad);
    });

    tom.hidden = kurs.length > 0;

    const ider = kurs.filter((k) => k.status === "PUBLISERT").map((k) => k.id);
    document.getElementById("tall-kurs").textContent = ider.length;
    document.getElementById("tall-pameldte").textContent =
      pameldinger.filter((p) => ider.includes(p.kursId) && p.status === "pameldt").length;
    document.getElementById("tall-venteliste").textContent =
      pameldinger.filter((p) => ider.includes(p.kursId) && p.status === "venteliste").length;
  }

  document.getElementById("nytt-kurs").addEventListener("click", () =>
    apneDialog(dialog, "Nytt kurs", { status: "PUBLISERT" })
  );

  kobleBilde(dialog);

  kobleDialog(dialog, (data) => {
    const id = Number(data.id);
    const nytt = {
      tittel: data.tittel.trim(), tema: data.tema, lokallag: data.lokallag, status: data.status,
      beskrivelse: data.beskrivelse.trim(), dato: data.dato, starttid: data.starttid, sluttid: data.sluttid,
      varighet: data.varighet.trim(), sted: data.sted.trim(), kursleder: data.kursleder.trim(),
      malgruppe: data.malgruppe, niva: data.niva, utstyr: data.utstyr.trim(),
      medlemspris: Number(data.medlemspris), ordinaerpris: Number(data.ordinaerpris), plasser: Number(data.plasser),
      kontaktperson: data.kontaktperson, bilde: data.bilde, bildeAlt: data.bildeAlt.trim()
    };

    const ny = id
      ? kurs.map((k) => (k.id === id ? { ...k, ...nytt } : k))
      : [...kurs, { id: nesteId(kurs), ...nytt }];

    if (!lagreListe("kurs", ny)) return "Kunne ikke lagre. Bildet kan være for stort for nettleseren.";
    kurs = ny;
    visKurs();
  });

  visKurs();
}

if (adminSide === "nyheter") {
  let nyheter = hentListe("nyheter", DEMO_NYHETER);
  const rader = document.getElementById("nyhet-rader");
  const tom = document.getElementById("nyhet-tom");
  const dialog = document.getElementById("nyhet-dialog");

  function visNyheter() {
    rader.replaceChildren();
    [...nyheter].sort((a, b) => b.dato.localeCompare(a.dato)).forEach((n) => {
      const rad = document.createElement("tr");

      const rediger = lagKnapp("Rediger", n.tittel);
      rediger.addEventListener("click", () => apneDialog(dialog, "Rediger nyhet", n));

      const slett = lagSlettKnapp(n.tittel, () => {
        nyheter = nyheter.filter((rad) => rad.id !== n.id);
        lagreListe("nyheter", nyheter);
        visNyheter();
      });

      const tittel = lagCelle(n.tittel, "th");
      tittel.scope = "row";
      rad.append(lagCelle(lagMiniBilde(n)), tittel, lagCelle(lagDato(n.dato)), lagCelle(n.lokallag), lagHandlinger(rediger, slett));
      rader.append(rad);
    });

    tom.hidden = nyheter.length > 0;
  }

  document.getElementById("ny-nyhet").addEventListener("click", () =>
    apneDialog(dialog, "Ny nyhet", { dato: new Date().toISOString().slice(0, 10) })
  );

  kobleBilde(dialog);

  kobleDialog(dialog, (data) => {
    const id = Number(data.id);
    const nytt = {
      tittel: data.tittel.trim(), dato: data.dato, lokallag: data.lokallag,
      ingress: data.ingress.trim(), tekst: data.tekst.trim(), bilde: data.bilde, bildeAlt: data.bildeAlt.trim()
    };

    const ny = id
      ? nyheter.map((n) => (n.id === id ? { ...n, ...nytt } : n))
      : [...nyheter, { id: nesteId(nyheter), ...nytt }];

    if (!lagreListe("nyheter", ny)) return "Kunne ikke lagre. Bildet kan være for stort for nettleseren.";
    nyheter = ny;
    visNyheter();
  });

  visNyheter();
}

if (adminSide === "pameldinger") {
  const kurs = hentListe("kurs", DEMO_KURS);
  let pameldinger = hentListe("pameldinger", DEMO_PAMELDINGER);
  const rader = document.getElementById("pamelding-rader");
  const tom = document.getElementById("pamelding-tom");
  const antall = document.getElementById("pamelding-antall");
  const filter = document.getElementById("filter-kurs");

  kurs.forEach((k) => filter.add(new Option(k.tittel, k.id)));

  const valgtKurs = new URLSearchParams(location.search).get("kurs");
  if (valgtKurs) filter.value = valgtKurs;

  const kursNavn = (id) => (kurs.find((k) => k.id === id) || {}).tittel || "Slettet kurs";

  function visPameldinger() {
    rader.replaceChildren();
    const kursId = Number(filter.value);
    const synlige = pameldinger
      .filter((p) => !kursId || p.kursId === kursId)
      .sort((a, b) => a.status.localeCompare(b.status) || a.navn.localeCompare(b.navn));

    synlige.forEach((p) => {
      const rad = document.createElement("tr");

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

  const dialog = document.getElementById("pamelding-dialog");
  const skjema = document.getElementById("pamelding-skjema");
  const velgKurs = document.getElementById("pam-kurs");
  const plasserTekst = document.getElementById("pam-plasser");
  const melding = document.getElementById("pamelding-melding");

  kurs.filter((k) => k.status !== "AVLYST").forEach((k) => velgKurs.add(new Option(k.tittel, k.id)));

  const antallPameldt = (id) =>
    pameldinger.filter((p) => p.kursId === id && p.status === "pameldt").length;

  function visPlasser() {
    const k = kurs.find((rad) => rad.id === Number(velgKurs.value));
    if (!k) {
      plasserTekst.textContent = "";
      return;
    }
    const ledige = k.plasser - antallPameldt(k.id);
    plasserTekst.textContent = ledige > 0
      ? ledige + " av " + k.plasser + " plasser ledige."
      : "Kurset er fullt. Personen blir satt på venteliste.";
  }
  velgKurs.addEventListener("change", visPlasser);

  document.getElementById("ny-pamelding").addEventListener("click", () => {
    apneDialog(dialog, "Legg til påmelding", { kursId: filter.value });
    visPlasser();
  });

  kobleDialog(dialog, (data) => {
    const kursId = Number(data.kursId);
    const epost = data.epost.trim().toLowerCase();

    if (pameldinger.some((p) => p.kursId === kursId && p.epost.toLowerCase() === epost)) {
      return "Denne e-posten er allerede påmeldt dette kurset.";
    }

    const k = kurs.find((rad) => rad.id === kursId);
    const status = antallPameldt(kursId) < k.plasser ? "pameldt" : "venteliste";
    const navn = data.fornavn.trim() + " " + data.etternavn.trim();

    pameldinger.push({
      id: nesteId(pameldinger), kursId: kursId, navn: navn, epost: epost,
      telefon: data.telefon.trim(), fodselsar: data.fodselsar.trim(), medlem: data.medlem === "ja",
      kommentar: data.kommentar.trim(), status: status, manuell: true
    });
    lagreListe("pameldinger", pameldinger);

    filter.value = String(kursId);
    visPameldinger();
    melding.textContent = status === "pameldt"
      ? navn + " er påmeldt " + k.tittel + "."
      : k.tittel + " er fullt. " + navn + " er satt på venteliste.";
    melding.hidden = false;
  });
}
