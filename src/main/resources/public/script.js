const menyknapp = document.querySelector(".menyknapp");
const mobilmeny = document.getElementById("mobilmeny");

menyknapp.addEventListener("click", function () {
  const erApen = menyknapp.getAttribute("aria-expanded") === "true";

  menyknapp.setAttribute("aria-expanded", String(!erApen));
  menyknapp.setAttribute("aria-label", erApen ? "Åpne meny" : "Lukk meny");
  mobilmeny.hidden = erApen;
});

const kursfilter = document.querySelector(".kursfilter");

if (kursfilter) {
  const sokefelt = document.getElementById("kurs-sok");
  const temaValg = document.getElementById("tema-filter");
  const lokallagValg = document.getElementById("lokallag-filter");
  const antall = document.getElementById("antall-kurs");
  const ingenTreff = document.getElementById("ingen-treff");

  const adresse = new URLSearchParams(window.location.search);
  sokefelt.value = adresse.get("sok") || "";
  temaValg.value = adresse.get("tema") || "";
  lokallagValg.value = adresse.get("lokallag") || "";

  function filtrerKurs() {
    const sok = sokefelt.value.trim().toLowerCase();
    const tema = temaValg.value;
    const lokallag = lokallagValg.value;
    let synlige = 0;

    //Mens kursene hentes, eller hvis noe gikk galt, står det en melding i listen.
    //Da skal verken antallet eller «ingen treff» vises.
    const visesMelding = document.querySelector("#kursliste > .liste-melding") !== null;
    antall.closest("p").hidden = visesMelding;
    if (visesMelding) {
      ingenTreff.hidden = true;
      return;
    }

    document.querySelectorAll("#kursliste > li").forEach(function (li) {
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

  kursfilter.addEventListener("input", filtrerKurs);
  kursfilter.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();
    filtrerKurs();
  });

  filtrerKurs();
}

function fyllKurs(kurs) {
  const kr = function (tall) { return tall.toLocaleString("nb-NO") + " kr"; };
  const verdier = {
    tittel: kurs.tittel, tema: kurs.tema, lokallag: kurs.lokallag, lagnavn: kurs.lokallag + " Husflidslag",
    dato: new Date(kurs.dato + "T12:00").toLocaleDateString("nb-NO", { day: "numeric", month: "long", year: "numeric" }),
    tid: kurs.tid, varighet: kurs.varighet, sted: kurs.sted,
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

const meldPaKnapp = document.getElementById("meld-pa-knapp");

if (meldPaKnapp) {
  visKursside();
}

//Skjuler det som mangler i databasen, så siden ikke viser tomme overskrifter og rader:
//«Om kurset» og «Hva du bør ta med» uten tekst, og rader i faktaboksene uten verdi.
function skjulTommeFelt() {
  document.querySelectorAll(".om-kurset, .ta-med").forEach(function (seksjon) {
    const tekst = seksjon.querySelector("[data-kurs]");
    seksjon.hidden = !tekst.textContent.trim();
  });

  document.querySelectorAll(".fakta dd, .info-list dd").forEach(function (dd) {
    const tom = !dd.textContent.trim();
    dd.hidden = tom;
    const dt = dd.previousElementSibling;
    if (dt && dt.tagName === "DT") dt.hidden = tom;
  });
}

async function visKursside() {
  const kursId = Number(new URLSearchParams(window.location.search).get("id"));
  const tilbake = { href: "kurs.html", tekst: "Se alle kurs" };

  let kurs;
  try {
    kurs = await hentKursFraApi(kursId, { kastVedFeil: true });
  } catch (feil) {
    visSidemelding("Kurset kan ikke vises akkurat nå", "Vi klarte ikke å hente kurset. Prøv igjen om litt.", "Kurset kan ikke vises", tilbake);
    return;
  }

  if (!kurs) {
    visSidemelding("Vi fant ikke kurset", "Kurset finnes ikke, eller det er ikke lenger åpent for påmelding.", "Fant ikke kurset", tilbake);
    return;
  }

  //Kurset er hentet: fjern «Henter kurset …» og vis innholdet
  document.getElementById("kurs-laster").remove();
  document.querySelector(".kursdetalj").hidden = false;
  document.querySelector("aside.pamelding").hidden = false;

  fyllKurs(kurs);
  skjulTommeFelt();
  document.title = kurs.tittel + " – Østfold Husflidslag";
  document.querySelector(".brodsmuler [aria-current]").href = "kurs-detalj.html?id=" + kursId;

  const hovedbilde = document.querySelector(".kursdetalj-bilde img");
  if (hovedbilde) {
    hovedbilde.src = bildeAdresse(kurs.bildesti) || "bilder/hero.jpg";
    hovedbilde.alt = kurs.bildealt || "";
  }

  visKontaktperson(kurs);

  const maler = document.getElementById("ledige-plasser");
  maler.max = kurs.plasser;
  maler.value = kurs.ledige;
  maler.textContent = kurs.ledige + " av " + kurs.plasser;

  meldPaKnapp.href = "pamelding.html?kurs=" + kursId;

  let andre = [];
  try {
    andre = (await hentJson("/api/kurs/kommende"))
      .filter(function (k) { return k.id !== kursId && k.ledigePlasser > 0; })
      .sort(function (a, b) { return (b.tema === kurs.tema) - (a.tema === kurs.tema); });
  } catch (feil) {
    console.error(feil);
  }
  visAndreKurs(andre.slice(0, 2));

  if (kurs.ledige === 0) {
    document.querySelector("main").classList.add("kurs-fullt");
    document.querySelectorAll(".fullt-merke, .fullt-etikett, .fullt-banner").forEach(function (felt) {
      felt.hidden = false;
    });

    document.querySelector(".ledige label").textContent = "Påmeldte";
    document.querySelector(".ledige span").textContent = kurs.plasser + " av " + kurs.plasser + " – fullt";
    maler.value = kurs.plasser;
    maler.textContent = "Fullt";

    meldPaKnapp.textContent = "Sett meg på venteliste";
    document.getElementById("venteliste-info").hidden = false;

    const liste = document.getElementById("alternativ-liste");
    liste.replaceChildren();
    andre.slice(0, 3).forEach(function (k) {
      const lenke = document.createElement("a");
      lenke.href = "kurs-detalj.html?id=" + k.id;
      const tittel = document.createElement("strong");
      tittel.textContent = k.tittel;
      const info = document.createElement("span");
      info.textContent = kortnavn(k.lokallag) + " · " + k.ledigePlasser + " ledige plasser";
      lenke.append(tittel, info);
      const li = document.createElement("li");
      li.append(lenke);
      liste.append(li);
    });
    document.getElementById("ledige-alternativ").hidden = andre.length === 0;
  }
}

function visKontaktperson(kurs) {
  const boks = document.querySelector(".kurs-kontakt");
  if (!boks) return;
  const kontakt = kurs.kontakt;
  if (!kontakt) {
    boks.hidden = true;
    return;
  }

  const bilde = boks.querySelector("img");
  if (bilde) {
    if (kontakt.bilde) {
      bilde.src = bildeAdresse(kontakt.bilde);
      bilde.onerror = function () { bilde.hidden = true; };
    } else {
      bilde.hidden = true;
    }
  }

  boks.querySelector(".kurs-kontakt-navn").textContent = kontakt.navn;
  boks.querySelector(".kurs-kontakt-rolle").textContent =
    [kontakt.verv, fulltLagnavn(kurs.lokallag)].filter(Boolean).join(", ");

  const liste = boks.querySelector("ul");
  liste.replaceChildren();

  if (kontakt.telefon) {
    const tlf = document.createElement("a");
    tlf.href = "tel:" + kontakt.telefon.replace(/\s/g, "");
    const skjult = document.createElement("span");
    skjult.className = "skjult";
    skjult.textContent = " (ring " + kontakt.navn + ")";
    tlf.append(kontakt.telefon, skjult);
    const li = document.createElement("li");
    li.append(tlf);
    liste.append(li);
  }

  if (kontakt.epost) {
    const epost = document.createElement("a");
    epost.href = "mailto:" + kontakt.epost;
    epost.textContent = kontakt.epost;
    const li = document.createElement("li");
    li.append(epost);
    liste.append(li);
  }
}

function visAndreKurs(andre) {
  const seksjon = document.querySelector(".andre-kurs");
  if (!seksjon) return;
  if (andre.length === 0) {
    seksjon.hidden = true;
    return;
  }

  const liste = seksjon.querySelector("ul");
  liste.replaceChildren();
  andre.forEach(function (k) {
    const lenke = document.createElement("a");
    lenke.href = "kurs-detalj.html?id=" + k.id;
    const lag = document.createElement("span");
    lag.textContent = kortnavn(k.lokallag);
    const tittel = document.createElement("strong");
    tittel.textContent = k.tittel;
    const tid = document.createElement("time");
    if (k.dato) tid.dateTime = k.dato;
    tid.textContent = datoTekst(k.dato);
    lenke.append(lag, tittel, tid);
    const li = document.createElement("li");
    li.append(lenke);
    liste.append(li);
  });
}

const skjema = document.getElementById("pamelding-skjema");

if (skjema) {
  visPameldingsskjema();
}

async function visPameldingsskjema() {
  const feilmelding = document.getElementById("skjema-feil");
  const prisboks = document.querySelector(".din-pris");
  const dinPris = document.getElementById("din-pris");
  const medlemsnummer = document.querySelectorAll(".medlemsnummer");

  function oppdaterMedlemskap() {
    const valgt = skjema.querySelector('input[name="medlem"]:checked');
    if (!valgt) return;
    const erMedlem = valgt.value === "ja";

    dinPris.textContent = erMedlem ? prisboks.dataset.medlem : prisboks.dataset.ordinaer;
    medlemsnummer.forEach(function (felt) {
      felt.hidden = !erMedlem;
    });
  }

  let lagret = hentPamelding();
  if (lagret && lagret.bekreftet) {
    slettPamelding();
    lagret = null;
  }

  const adresse = new URLSearchParams(window.location.search);
  const kursId = Number(adresse.get("kurs") || (lagret && lagret.kursId));
  const tilbake = { href: "kurs.html", tekst: "Se alle kurs" };

  let kurs;
  try {
    kurs = await hentKursFraApi(kursId, { kastVedFeil: true });
  } catch (feil) {
    visSidemelding("Påmeldingen kan ikke vises akkurat nå", "Vi klarte ikke å hente kurset. Prøv igjen om litt.", "Påmeldingen kan ikke vises", tilbake);
    return;
  }

  if (!kurs) {
    visSidemelding("Vi fant ikke kurset", "Gå tilbake til kursoversikten og velg kurset du vil melde deg på.", "Fant ikke kurset", tilbake);
    return;
  }

  //Kurset er hentet: fjern «Henter kurset …» og vis skjemaet og oppsummeringen
  document.getElementById("kurs-laster").remove();
  document.querySelector(".pameldingsskjema").hidden = false;
  document.querySelector(".kurs-oppsummering").hidden = false;

  //Fødselsår kan ikke være senere enn i år
  skjema.elements.fodselsar.max = new Date().getFullYear();

  fyllKurs(kurs);
  skjulTommeFelt();
  document.title = "Påmelding: " + kurs.tittel + " – Østfold Husflidslag";
  document.getElementById("brodsmule-kurs").href = "kurs-detalj.html?id=" + kursId;
  prisboks.dataset.medlem = kurs.medlemspris.toLocaleString("nb-NO") + " kr";
  prisboks.dataset.ordinaer = kurs.ordinaerpris.toLocaleString("nb-NO") + " kr";

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

  skjema.addEventListener("input", function (hendelse) {
    if (hendelse.target.checkValidity()) {
      hendelse.target.removeAttribute("aria-invalid");
    }
  });

  skjema.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();

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

const bekreftSkjema = document.getElementById("bekreft-skjema");

if (bekreftSkjema) {
  visBekreftelse();
}

async function visBekreftelse() {
  const pamelding = hentPamelding();
  const knapp = document.getElementById("bekreft-knapp");

  if (!pamelding || !pamelding.nokkel) {
    bekreftSkjema.hidden = true;
    document.getElementById("ingen-pamelding").hidden = false;
    return;
  }

  const feilmelding = document.createElement("p");
  feilmelding.className = "skjema-feil";
  feilmelding.setAttribute("role", "alert");
  feilmelding.hidden = true;
  knapp.before(feilmelding);

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

  document.querySelector("#tilbake a").href = "pamelding.html?kurs=" + pamelding.kursId;

  //Kursnavn, dato og sted hentes fra API-et. Går det ikke, sier vi det i stedet for å vise feil kurs.
  const kurs = await hentKursFraApi(pamelding.kursId);
  if (kurs) {
    fyllKurs(kurs);
    document.getElementById("bekreft-kursinfo").hidden = false;
  } else {
    document.getElementById("bekreft-kurs").textContent = "Vi klarte ikke å hente kurset akkurat nå";
  }

  if (pamelding.venteliste) {
    document.getElementById("bekreft-tittel").textContent = "Bekreft plass på ventelisten";
    knapp.textContent = "Sett meg på venteliste";
    document.title = "Bekreft venteliste – Østfold Husflidslag";
  }

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

      pamelding.bekreftet = true;
      pamelding.kvittering = innhold;
      lagrePamelding(pamelding);
      window.location.href = bekreftSkjema.getAttribute("action");
    } catch (feil) {
      console.error(feil);
      feilmelding.textContent = "Fikk ikke kontakt med serveren. Sjekk nettet og trykk på knappen igjen.";
      feilmelding.hidden = false;
    } finally {
      sender = false;
      knapp.disabled = false;
    }
  });
}

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

const pameldt = document.getElementById("pameldt");

if (pameldt) {
  const pamelding = hentPamelding();
  const kvittering = pamelding && pamelding.bekreftet ? pamelding.kvittering : null;

  if (kvittering) {
    pameldt.hidden = false;
    document.getElementById("pameldt-epost").textContent = pamelding.epost;
    fyllKvittering(kvittering);

    document.getElementById("pameldt-melding").textContent =
      "Påmeldingen til " + kvittering.kursTittel + " er registrert (referanse " + kvittering.referanse + ")";

    if (kvittering.status === "venteliste") {
      document.getElementById("pameldt-tittel").textContent = "Du står på venteliste";
      document.getElementById("pameldt-melding").textContent =
        "Kurset " + kvittering.kursTittel + " er fullt. Du står på ventelisten (referanse " + kvittering.referanse +
        "). Når noen melder seg av, får førstemann på ventelisten plassen og beskjed på e-post. Du er registrert";
      document.title = "Du står på venteliste – Østfold Husflidslag";
    }

    lagrePamelding({ bekreftet: true, epost: pamelding.epost, kvittering: kvittering });
  } else {
    document.getElementById("ingen-pamelding").hidden = false;
  }
}

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
    if (verdier[felt.dataset.kurs] !== undefined) felt.textContent = verdier[felt.dataset.kurs] || "";
    if (felt.dataset.kurs === "dato") felt.dateTime = kvittering.dato;
  });

  //Linjer med «A · B» i kurskortet: vis bare det som finnes, uten løse prikker
  document.querySelectorAll(".pameldt-kurs p").forEach(function (linje) {
    const deler = [...linje.querySelectorAll("[data-kurs]")].filter(function (felt) { return felt.textContent.trim(); });
    if (deler.length === 0) linje.hidden = true;
    else if (deler.length === 1) linje.replaceChildren(deler[0]);
  });
}

const artikkel = document.getElementById("artikkel");

if (artikkel) {
  //Delingsknappene settes opp først. visNyhet() kan bytte ut hele siden med en feilmelding.
  const delFacebook = document.getElementById("del-facebook");
  delFacebook.href = "https://www.facebook.com/sharer/sharer.php?u=" + encodeURIComponent(window.location.href);

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

  visNyhet();
}

const medlemSkjema = document.getElementById("medlem-skjema");

if (medlemSkjema) {
  const medlemFeil = document.getElementById("medlem-feil");
  const medlemTakk = document.getElementById("medlem-takk");

  medlemSkjema.addEventListener("input", function (hendelse) {
    if (hendelse.target.checkValidity()) {
      hendelse.target.removeAttribute("aria-invalid");
    }
  });

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

    const navn = document.getElementById("medlem-navn").value.trim();
    const lokallag = document.getElementById("medlem-lokallag");
    const lagNavn = lokallag.options[lokallag.selectedIndex].text;

    medlemTakk.textContent = "Takk, " + navn + "! " + lagNavn + " Husflidslag tar kontakt med deg om medlemskapet.";
    medlemSkjema.hidden = true;
    medlemTakk.hidden = false;
    medlemTakk.focus();
  });
}

function lagrePamelding(data) {
  try {
    sessionStorage.setItem("pamelding", JSON.stringify(data));
  } catch (feil) {
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
  }
}

async function hentJson(adresse) {
  const svar = await fetch(adresse);
  if (!svar.ok) {
    const feil = new Error("Feil fra serveren: " + svar.status);
    feil.status = svar.status; //så vi kan skille «finnes ikke» (404) fra «serveren er nede»
    throw feil;
  }
  return svar.json();
}

//Bytter ut innholdet i en liste med én melding over hele bredden (tom liste eller feil)
function visMelding(liste, tekst) {
  const li = document.createElement("li");
  li.className = "liste-melding";
  const p = document.createElement("p");
  p.textContent = tekst;
  li.append(p);
  liste.replaceChildren(li);
}

//Bytter ut hele innholdet i <main> med en overskrift, en forklaring og en knapp tilbake.
//Brukes når et kurs eller en nyhet ikke finnes, eller serveren ikke svarer.
function visSidemelding(overskrift, forklaring, sidetittel, tilbake) {
  const tittel = document.createElement("h1");
  tittel.textContent = overskrift;
  const tekst = document.createElement("p");
  tekst.textContent = forklaring;
  const lenke = document.createElement("a");
  lenke.href = tilbake.href;
  lenke.className = "knapp knapp-primar";
  lenke.textContent = tilbake.tekst;
  document.querySelector("main").replaceChildren(tittel, tekst, lenke);
  document.title = sidetittel + " – Østfold Husflidslag";
}

function visFeil(lister, tekst) {
  lister.forEach(function (liste) {
    if (liste) visMelding(liste, tekst);
  });
}

function kortnavn(navn) {
  return (navn || "").replace(" Husflidslag", "");
}

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

//Gir null hvis kurset ikke finnes. Med kastVedFeil: true kastes feilen videre
//når serveren ikke svarer, så siden kan vise en annen melding enn «finnes ikke».
async function hentKursFraApi(id, { kastVedFeil = false } = {}) {
  if (!Number.isInteger(id) || id <= 0) return null;
  try {
    return tilKursVisning(await hentJson("/api/kurs/" + id));
  } catch (feil) {
    console.error(feil);
    if (kastVedFeil && feil.status !== 404 && feil.status !== 400) throw feil;
    return null;
  }
}

function bildeAdresse(sti) {
  if (!sti) return "";
  return sti.includes("/") ? sti : "bilder/" + sti;
}

function fulltLagnavn(navn) {
  return kortnavn(navn) + " Husflidslag";
}

//«Finn oss» på kontakt.html: ett adressekort per lokallag som har adresse.
//Kortene legges foran Østfold Husflidslag, som står fast i HTML-en.
function fyllFinnOss(lagene) {
  const liste = document.getElementById("finn-oss-liste");
  if (!liste) return;
  liste.querySelectorAll(".liste-melding").forEach(function (li) { li.remove(); });
  const fylket = document.getElementById("finn-oss-fylke");

  lagene.filter(function (lag) { return lag.adresse; }).forEach(function (lag) {
    const adresse = document.createElement("address");
    const navn = document.createElement("strong");
    navn.textContent = fulltLagnavn(lag.navn);
    const gate = document.createElement("span");
    gate.textContent = lag.adresse;
    adresse.append(navn, gate);

    //Telefon og e-post på samme linje, med · mellom
    const kontakt = document.createElement("span");
    if (lag.telefon) {
      const tlf = document.createElement("a");
      tlf.href = "tel:" + lag.telefon.replace(/\s/g, "");
      tlf.textContent = lag.telefon;
      kontakt.append(tlf);
    }
    if (lag.epost) {
      const epost = document.createElement("a");
      epost.href = "mailto:" + lag.epost;
      epost.textContent = lag.epost;
      if (kontakt.childNodes.length) kontakt.append(" · ");
      kontakt.append(epost);
    }
    if (kontakt.childNodes.length) adresse.append(kontakt);

    const li = document.createElement("li");
    li.append(adresse);
    liste.insertBefore(li, fylket);
  });
}

async function visLokallag() {
  let lagene;
  try {
    lagene = await hentJson("/api/lokallag");
  } catch (feil) {
    console.error(feil);
    visFeil(document.querySelectorAll("ul.lokallag, ul.lag-liste"), "Kunne ikke hente lokallagene akkurat nå.");
    fyllFinnOss([]); //Østfold Husflidslag står fortsatt, så siden har alltid en adresse
    return;
  }

  fyllFinnOss(lagene);

  document.querySelectorAll("#lokallag, #lokallag-mobil").forEach(function (velger) {
    velger.replaceChildren();
    lagene.forEach(function (lag) {
      velger.add(new Option(kortnavn(lag.navn), lag.slug));
    });
  });

  const filter = document.getElementById("lokallag-filter");
  if (filter) {
    const valgt = new URLSearchParams(window.location.search).get("lokallag") || "";
    filter.replaceChildren(new Option("Alle lokallag", ""));
    lagene.forEach(function (lag) {
      filter.add(new Option(kortnavn(lag.navn), lag.slug));
    });
    filter.value = valgt;
    filter.dispatchEvent(new Event("input", { bubbles: true }));
  }

  const liste = document.querySelector("ul.lokallag");
  if (liste && lagene.length === 0) {
    visMelding(liste, "Det er ingen lokallag å vise.");
  } else if (liste) {
    liste.replaceChildren();
    lagene.forEach(function (lag) {
      const lenke = document.createElement("a");
      lenke.href = "kurs.html?lokallag=" + encodeURIComponent(lag.slug);
      const sterk = document.createElement("strong");
      sterk.textContent = kortnavn(lag.navn);
      lenke.append(sterk, " Husflidslag");

      const li = document.createElement("li");
      li.append(lenke);
      liste.append(li);
    });
  }

  const kortliste = document.querySelector("ul.lag-liste");
  if (kortliste && lagene.length === 0) {
    visMelding(kortliste, "Det er ingen lokallag å vise.");
  } else if (kortliste) {
    kortliste.replaceChildren();
    lagene.forEach(function (lag) {
      const kort = document.createElement("article");
      kort.className = "kort lag-kort";

      if (lag.bildesti) {
        const figur = document.createElement("figure");
        const bilde = document.createElement("img");
        bilde.src = bildeAdresse(lag.bildesti);
        bilde.alt = "";
        figur.append(bilde);
        kort.append(figur);
      }

      const tittel = document.createElement("h3");
      tittel.textContent = fulltLagnavn(lag.navn);

      //Beskrivelsen vises bare hvis laget har en i databasen
      const tekst = document.createElement("p");
      tekst.textContent = lag.beskrivelse || "";
      tekst.hidden = !lag.beskrivelse;

      const lenke = document.createElement("a");
      lenke.href = "kurs.html?lokallag=" + encodeURIComponent(lag.slug);
      lenke.className = "les-mer";
      const skjult = document.createElement("span");
      skjult.className = "skjult";
      skjult.textContent = " hos " + fulltLagnavn(lag.navn);
      lenke.append("Se kurs", skjult);

      kort.append(tittel, tekst, lenke);
      const li = document.createElement("li");
      li.append(kort);
      kortliste.append(li);
    });
  }
}

visLokallag();

//"Søm" blir til "som", "Andre håndverk" til "andre-handverk". Brukes i tema-filteret og i adressen (?tema=).
function temaVerdi(tema) {
  return (tema || "").trim().toLowerCase()
    .replace(/ø/g, "o").replace(/æ/g, "ae").replace(/å/g, "a")
    .replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
}

//Bygger tema-filteret ut fra temaene til kursene, sortert alfabetisk.
//Valget fra adressen (?tema=) settes etterpå, fordi valget ikke finnes før listen er bygget.
function byggTemafilter(kurs) {
  const valg = document.getElementById("tema-filter");
  if (!valg) return;

  const temaer = new Map();
  kurs.forEach(function (k) {
    if (k.tema) temaer.set(temaVerdi(k.tema), k.tema);
  });

  valg.replaceChildren(new Option("Alle temaer", ""));
  [...temaer].sort(function (a, b) { return a[1].localeCompare(b[1], "nb"); })
    .forEach(function ([verdi, navn]) { valg.add(new Option(navn, verdi)); });

  const fraAdressen = new URLSearchParams(window.location.search).get("tema") || "";
  valg.value = temaer.has(fraAdressen) ? fraAdressen : "";
}

function kroner(tall) {
  return Number(tall).toLocaleString("nb-NO") + " kr";
}

function datoTekst(dato) {
  if (!dato) return "Dato kommer";
  return new Date(dato + "T12:00").toLocaleDateString("nb-NO", { day: "numeric", month: "long", year: "numeric" });
}

function plasser(k) {
  if (k.ledigePlasser === 0) return { tekst: "Fullt", klasse: "plasser-fullt" };
  const tekst = k.ledigePlasser + " av " + k.kapasitet + " ledige";
  return { tekst: tekst, klasse: k.ledigePlasser <= 3 ? "plasser-faa" : "plasser-ledig" };
}

function lagKurskort(k, slugFor) {
  const li = document.createElement("li");
  li.dataset.tema = temaVerdi(k.tema);
  li.dataset.lokallag = slugFor[kortnavn(k.lokallag).toLowerCase()] || "";

  const kort = document.createElement("article");
  kort.className = "kort kurs";

  const figur = document.createElement("figure");
  const bilde = document.createElement("img");
  bilde.src = bildeAdresse(k.bildesti) || "bilder/hero.jpg";
  bilde.alt = k.bildealt || "";
  figur.append(bilde);

  const tema = document.createElement("p");
  tema.className = "kurs-tema";
  tema.textContent = k.tema;

  const lag = document.createElement("p");
  lag.className = "kurs-lokallag";
  lag.textContent = fulltLagnavn(k.lokallag);

  const tittel = document.createElement("h3");
  tittel.textContent = k.tittel;

  const info = document.createElement("ul");
  info.className = "kurs-info";
  const datoLi = document.createElement("li");
  datoLi.className = "dato";
  const tid = document.createElement("time");
  if (k.dato) tid.dateTime = k.dato;
  tid.textContent = datoTekst(k.dato);
  datoLi.append(tid);
  const tidLi = document.createElement("li");
  tidLi.className = "tid";
  tidLi.textContent = [k.tidspunkt, k.varighet].filter(Boolean).join(" · ");
  info.append(datoLi, tidLi);

  const bunn = document.createElement("footer");
  const p = plasser(k);
  const plasserTekst = document.createElement("p");
  plasserTekst.className = "plasser " + p.klasse;
  plasserTekst.textContent = p.tekst;
  const pris = document.createElement("p");
  pris.className = "pris";
  const sterk = document.createElement("strong");
  sterk.textContent = kroner(k.medlemspris);
  pris.append(sterk, " for medlem");
  bunn.append(plasserTekst, pris);

  const lenke = document.createElement("a");
  lenke.href = "kurs-detalj.html?id=" + k.id;
  const fullt = k.ledigePlasser === 0;
  lenke.className = "knapp " + (fullt ? "knapp-gra" : "knapp-primar");
  const skjult = document.createElement("span");
  skjult.className = "skjult";
  skjult.textContent = ": " + k.tittel;
  lenke.append(fullt ? "Se kurset og ventelisten" : "Se kurset", skjult);

  kort.append(figur, tema, lag, tittel, info, bunn, lenke);
  li.append(kort);
  return li;
}

async function visKurs() {
  const kursliste = document.getElementById("kursliste");
  const erForside = /\/(index\.html)?$/.test(window.location.pathname);
  const forsideTittel = erForside ? document.getElementById("kurs-tittel") : null;
  const forsideliste = forsideTittel ? forsideTittel.closest("section").querySelector(".kortliste") : null;
  if (!kursliste && !forsideliste) return;

  let kurs, lagene;
  try {
    [kurs, lagene] = await Promise.all([hentJson("/api/kurs/kommende"), hentJson("/api/lokallag")]);
  } catch (feil) {
    console.error(feil);
    visFeil([kursliste, forsideliste], "Kunne ikke hente kursene akkurat nå. Prøv igjen om litt.");
    return;
  }

  const slugFor = {};
  lagene.forEach(function (lag) {
    slugFor[kortnavn(lag.navn).toLowerCase()] = lag.slug;
  });

  if (kursliste) {
    byggTemafilter(kurs);
    if (kurs.length === 0) visMelding(kursliste, "Det er ingen kommende kurs akkurat nå. Se innom igjen snart!");
    else kursliste.replaceChildren(...kurs.map(function (k) { return lagKurskort(k, slugFor); }));
    document.querySelector(".kursfilter").dispatchEvent(new Event("input"));
  }

  if (forsideliste) {
    if (kurs.length === 0) visMelding(forsideliste, "Det er ingen kommende kurs akkurat nå. Se innom igjen snart!");
    else forsideliste.replaceChildren(...kurs.slice(0, 3).map(function (k) { return lagKurskort(k, slugFor); }));
  }
}

visKurs();

function lagNyhetskort(n) {
  const kort = document.createElement("article");
  kort.className = "kort nyhet";

  const figur = document.createElement("figure");
  const bilde = document.createElement("img");
  bilde.src = bildeAdresse(n.bildesti) || "bilder/hero.jpg";
  bilde.alt = n.bildealt || "";
  figur.append(bilde);

  const meta = document.createElement("p");
  meta.className = "nyhet-meta";
  const tid = document.createElement("time");
  tid.dateTime = n.dato;
  tid.textContent = n.datoTekst;
  const lag = document.createElement("span");
  lag.textContent = kortnavn(n.lokallag);
  meta.append(tid, " ", lag);

  const tittel = document.createElement("h3");
  tittel.textContent = n.tittel;

  const ingress = document.createElement("p");
  ingress.textContent = n.ingress;

  const lenke = document.createElement("a");
  lenke.href = "nyhet.html?id=" + n.id;
  lenke.className = "les-mer";
  const skjult = document.createElement("span");
  skjult.className = "skjult";
  skjult.textContent = ": " + n.tittel;
  lenke.append("Les mer", skjult);

  kort.append(figur, meta, tittel, ingress, lenke);
  const li = document.createElement("li");
  li.append(kort);
  return li;
}

function fyllNyhetsliste(liste, nyheter) {
  if (nyheter.length === 0) {
    visMelding(liste, "Det er ingen nyheter ennå.");
    return;
  }
  liste.replaceChildren(...nyheter.map(lagNyhetskort));
}

async function visNyheter() {
  const aktuelt = document.querySelector("ul.nyhetsliste");
  const forsideTittel = document.getElementById("nytt-tittel");
  const forsideliste = forsideTittel ? forsideTittel.closest("section").querySelector(".kortliste") : null;
  if (!aktuelt && !forsideliste) return;

  let nyheter;
  try {
    nyheter = await hentJson("/api/nyheter");
  } catch (feil) {
    console.error(feil);
    visFeil([aktuelt, forsideliste], "Kunne ikke hente nyhetene akkurat nå. Prøv igjen om litt.");
    return;
  }

  if (aktuelt) fyllNyhetsliste(aktuelt, nyheter);
  if (forsideliste) fyllNyhetsliste(forsideliste, nyheter.slice(0, 3));
}

visNyheter();

async function visNyhet() {
  const id = Number(new URLSearchParams(window.location.search).get("id"));
  const tilbake = { href: "aktuelt.html", tekst: "Se alle nyheter" };
  let nyhet = null;
  if (Number.isInteger(id) && id > 0) {
    try {
      nyhet = await hentJson("/api/nyheter/" + id);
    } catch (feil) {
      console.error(feil);
      //404/400 betyr at nyheten ikke finnes. Andre feil betyr at serveren ikke svarer.
      if (feil.status !== 404 && feil.status !== 400) {
        visSidemelding("Nyheten kan ikke vises akkurat nå", "Vi klarte ikke å hente nyheten. Prøv igjen om litt.", "Nyheten kan ikke vises", tilbake);
        return;
      }
    }
  }

  if (!nyhet) {
    visSidemelding("Vi fant ikke nyheten", "Nyheten finnes ikke, eller den er ikke publisert.", "Fant ikke nyheten", tilbake);
    return;
  }

  //Nyheten er hentet: fjern «Henter nyheten …» og vis artikkelen
  document.getElementById("nyhet-laster").remove();
  document.getElementById("artikkel").hidden = false;

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

  const bilde = document.querySelector(".artikkel-bilde img");
  if (bilde) {
    if (nyhet.bildesti) {
      bilde.src = bildeAdresse(nyhet.bildesti);
      bilde.alt = nyhet.bildealt || "";
    } else {
      bilde.closest("figure").hidden = true;
    }
  }

  const tekst = document.getElementById("artikkel-tekst");
  tekst.replaceChildren();
  (nyhet.innhold || "").split(/\n\s*\n/).forEach(function (avsnitt) {
    if (!avsnitt.trim()) return;
    const p = document.createElement("p");
    p.textContent = avsnitt.trim();
    tekst.append(p);
  });
}

function lagPersonkort(p) {
  const kort = document.createElement("article");
  kort.className = "person";

  if (p.bilde) {
    const bilde = document.createElement("img");
    bilde.src = bildeAdresse(p.bilde);
    bilde.alt = "";
    bilde.onerror = function () { bilde.hidden = true; };
    kort.append(bilde);
  }

  const navn = document.createElement("h3");
  navn.textContent = p.navn;
  kort.append(navn);

  if (p.verv) {
    const rolle = document.createElement("p");
    rolle.className = "person-rolle";
    rolle.textContent = p.verv;
    kort.append(rolle);
  }

  if (p.sted) {
    const sted = document.createElement("p");
    sted.className = "person-sted";
    sted.textContent = p.sted;
    kort.append(sted);
  }

  const kontakt = document.createElement("ul");
  kontakt.className = "person-kontakt";

  if (p.telefon) {
    const tlf = document.createElement("a");
    tlf.href = "tel:" + p.telefon.replace(/\s/g, "");
    tlf.textContent = p.telefon;
    const li = document.createElement("li");
    li.append("Tlf: ", tlf);
    kontakt.append(li);
  }

  if (p.epost) {
    const epost = document.createElement("a");
    epost.href = "mailto:" + p.epost;
    epost.textContent = p.epost;
    const li = document.createElement("li");
    li.append("E-post: ", epost);
    kontakt.append(li);
  }

  kort.append(kontakt);
  const li = document.createElement("li");
  li.append(kort);
  return li;
}

async function visKontaktpersoner() {
  const liste = document.querySelector("ul.personer");
  if (!liste) return;

  let personer;
  try {
    personer = await hentJson("/api/kontaktpersoner");
  } catch (feil) {
    console.error(feil);
    visFeil([liste], "Kunne ikke hente kontaktpersonene akkurat nå. Du kan kontakte oss på telefon eller e-post nederst på siden.");
    return;
  }

  if (personer.length === 0) {
    visMelding(liste, "Kontaktpersonene kommer snart. Du kan kontakte oss på telefon eller e-post nederst på siden.");
    return;
  }

  liste.replaceChildren(...personer.map(lagPersonkort));
}

visKontaktpersoner();