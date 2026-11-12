"use strict";

async function hentInnloggetBruker() {
  try {
    const svar = await fetch("/api/meg");
    if (!svar.ok) return null;
    return await svar.json();
  } catch (feil) {
    return null;
  }
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
  skjema.querySelectorAll('input[type="hidden"]').forEach((felt) => { felt.value = ""; });
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

  skjema.addEventListener("submit", async (hendelse) => {
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

    const knapp = skjema.querySelector('button[type="submit"]');
    if (knapp) knapp.disabled = true;
    let melding;
    try {
      melding = await vedLagring(Object.fromEntries(new FormData(skjema)));
    } finally {
      if (knapp) knapp.disabled = false;
    }
    if (melding) {
      feil.textContent = melding;
      feil.hidden = false;
      return;
    }
    dialog.close();
  });
}

const MAKS_BILDE_MB = 10;

function adminBilde(sti) {
  if (!sti) return "";
  return sti.includes("/") ? sti : "/bilder/" + sti;
}

function visBilde(dialog) {
  const skjult = dialog.querySelector('input[name="bilde"]');
  if (!skjult) return;
  const figur = dialog.querySelector(".bilde-forhandsvisning");
  const alt = dialog.querySelector('input[name="bildeAlt"]');
  figur.hidden = !skjult.value;
  figur.querySelector("img").src = skjult.value ? adminBilde(skjult.value) : "";
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
      URL.revokeObjectURL(bilde.src);
      lerret.toBlob(async (blob) => {
        const skjema = new FormData();
        skjema.append("bilde", blob, "bilde.jpg");
        try {
          const svar = await fetch("/api/admin/bilder", { method: "POST", body: skjema });
          const innhold = await svar.json().catch(() => ({}));
          if (!svar.ok) throw new Error(innhold.melding || "Kunne ikke laste opp bildet.");
          skjult.value = innhold.bildesti;
          feil.hidden = true;
          visBilde(dialog);
        } catch (e) {
          feil.textContent = e.message;
          feil.hidden = false;
          fil.value = "";
        }
      }, "image/jpeg", 0.82);
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
  img.src = adminBilde(rad.bilde);
  img.alt = "";
  img.className = "admin-mini-bilde";
  return img;
}

async function api(metode, sti, data) {
  const valg = { method: metode, headers: {} };
  if (data !== undefined) {
    valg.headers["Content-Type"] = "application/json";
    valg.body = JSON.stringify(data);
  }
  const svar = await fetch(sti, valg);
  if (svar.status === 401) {
    location.replace("admin.html");
    throw new Error("Du er ikke logget inn.");
  }
  const innhold = await svar.json().catch(() => ({}));
  if (!svar.ok) throw new Error(innhold.melding || "Noe gikk galt. Prøv igjen.");
  return innhold;
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
  const rader = document.getElementById("kurs-rader");
  const tom = document.getElementById("kurs-tom");
  const dialog = document.getElementById("kurs-dialog");
  const skjema = document.getElementById("kurs-skjema");
  const velgLag = document.getElementById("kurs-lokallag");
  const velgKontakt = document.getElementById("kurs-kontaktperson");

  const STATUS = { PUBLISERT: "Publisert", KLADD: "Kladd" };

  skjema.addEventListener("input", () => {
    const start = skjema.elements.starttid.value;
    const slutt = skjema.elements.sluttid;
    slutt.setCustomValidity(start && slutt.value && slutt.value <= start ? "Sluttid må være etter starttid" : "");
  });

  function sikreValg(select, verdi) {
    if (!verdi) return;
    const finnes = [...select.options].some((valg) => valg.value === String(verdi));
    if (!finnes) select.add(new Option(verdi, verdi));
  }

  async function fyllValg() {
    try {
      const [lagene, kontakter] = await Promise.all([
        api("GET", "/api/lokallag"),
        api("GET", "/api/admin/kontaktpersoner")
      ]);
      velgLag.replaceChildren(new Option("Velg lokallag", ""));
      lagene.forEach((lag) => velgLag.add(new Option(lag.navn, lag.id)));
      velgKontakt.replaceChildren(new Option("Velg kontaktperson", ""));
      kontakter.forEach((k) => velgKontakt.add(new Option(k.navn, k.id)));
    } catch (feil) {
      console.error(feil);
    }
  }

  function tilSkjema(k) {
    const [start, slutt] = (k.tidspunkt || "").split(/\s*[–-]\s*/);
    ["tema", "malgruppe", "niva"].forEach((navn) => sikreValg(skjema.elements[navn], k[navn]));
    return {
      id: k.id,
      tittel: k.tittel,
      tema: k.tema,
      lokallag: k.lokallagId == null ? "" : String(k.lokallagId),
      beskrivelse: k.beskrivelse,
      malgruppe: k.malgruppe,
      niva: k.niva,
      utstyr: k.utstyr,
      dato: k.dato || "",
      starttid: start || "",
      sluttid: slutt || "",
      varighet: k.varighet,
      sted: k.sted,
      medlemspris: k.medlemspris,
      ordinaerpris: k.ordinaerpris,
      plasser: k.kapasitet,
      kontaktperson: String(k.kontaktId),
      bilde: k.bildesti || "",
      bildeAlt: k.bildealt || "",
      status: k.status
    };
  }

  async function visTall() {
    try {
      const tall = await api("GET", "/api/admin/oversikt");
      document.getElementById("tall-kurs").textContent = tall.antallKurs;
      document.getElementById("tall-pameldte").textContent = tall.antallPameldte;
      document.getElementById("tall-venteliste").textContent = tall.antallVenteliste;
    } catch (feil) {
      console.error(feil);
    }
  }

  async function visKurs() {
    let kurs;
    try {
      kurs = await api("GET", "/api/admin/kurs");
    } catch (feil) {
      alert(feil.message);
      return;
    }

    rader.replaceChildren();
    kurs.forEach((k) => {
      const rad = document.createElement("tr");
      const pameldt = k.kapasitet - k.ledigePlasser;

      const tittel = document.createElement("th");
      tittel.scope = "row";
      const tema = document.createElement("span");
      tema.className = "admin-under";
      tema.textContent = k.tema;
      tittel.append(k.tittel, tema);

      const dato = document.createElement("td");
      const tid = document.createElement("span");
      tid.className = "admin-under";
      tid.textContent = k.tidspunkt;
      dato.append(k.dato ? lagDato(k.dato) : "Ingen dato", tid);

      const plasser = document.createElement("span");
      plasser.textContent = pameldt + " av " + k.kapasitet;
      if (k.ledigePlasser === 0) plasser.className = "admin-fullt";

      const status = document.createElement("span");
      status.className = "admin-status admin-status-" + k.status.toLowerCase();
      status.textContent = STATUS[k.status] || k.status;

      const rediger = lagKnapp("Rediger", k.tittel);
      rediger.addEventListener("click", () => apneDialog(dialog, "Rediger kurs", tilSkjema(k)));

      const slett = lagSlettKnapp(k.tittel, async () => {
        try {
          await api("DELETE", "/api/admin/kurs/" + k.id);
        } catch (feil) {
          alert(feil.message);
        }
        await oppdater();
      });

      rad.append(
        lagCelle(lagMiniBilde({ bilde: k.bildesti })),
        tittel,
        dato,
        lagCelle((k.lokallag || "").replace(" Husflidslag", "")),
        lagCelle(plasser),
        lagCelle(status),
        lagHandlinger(rediger, slett)
      );
      rader.append(rad);
    });

    tom.hidden = kurs.length > 0;
  }

  async function oppdater() {
    await Promise.all([visKurs(), visTall()]);
  }

  document.getElementById("nytt-kurs").addEventListener("click", () =>
    apneDialog(dialog, "Nytt kurs", { status: "PUBLISERT" })
  );

  kobleBilde(dialog);

  kobleDialog(dialog, async (data) => {
    const id = Number(data.id);
    const kurs = {
      tittel: data.tittel.trim(),
      beskrivelse: data.beskrivelse.trim(),
      dato: data.dato,
      tidspunkt: data.starttid + "–" + data.sluttid,
      varighet: data.varighet.trim(),
      sted: data.sted.trim(),
      lokallagId: data.lokallag ? Number(data.lokallag) : null,
      tema: data.tema,
      malgruppe: data.malgruppe,
      niva: data.niva,
      utstyr: (data.utstyr || "").trim() || "Ingen spesielt utstyr",
      medlemspris: Number(data.medlemspris),
      ordinaerpris: Number(data.ordinaerpris),
      kapasitet: Number(data.plasser),
      status: data.status,
      kontaktId: Number(data.kontaktperson),
      bildesti: data.bilde || "",
      bildealt: (data.bildeAlt || "").trim()
    };

    try {
      if (id) await api("PUT", "/api/admin/kurs/" + id, kurs);
      else await api("POST", "/api/admin/kurs", kurs);
    } catch (feil) {
      return feil.message;
    }
    await oppdater();
  });

  fyllValg().then(oppdater);
}

if (adminSide === "nyheter") {
  const rader = document.getElementById("nyhet-rader");
  const tom = document.getElementById("nyhet-tom");
  const dialog = document.getElementById("nyhet-dialog");
  const velgLag = document.getElementById("nyhet-lokallag");

  async function fyllLokallag() {
    try {
      const lagene = await api("GET", "/api/lokallag");
      velgLag.replaceChildren(new Option("Velg lokallag", ""), new Option("Hele Østfold", "fylket"));
      lagene.forEach((lag) => velgLag.add(new Option(lag.navn, lag.id)));
    } catch (feil) {
      console.error(feil);
    }
  }

  function tilSkjema(n) {
    return {
      id: n.id,
      tittel: n.tittel,
      dato: n.dato || "",
      lokallag: n.lokallagId == null ? "fylket" : String(n.lokallagId),
      ingress: n.ingress,
      tekst: n.innhold,
      bilde: n.bildesti || "",
      bildeAlt: n.bildealt || ""
    };
  }

  async function visNyheter() {
    let nyheter;
    try {
      nyheter = await api("GET", "/api/admin/nyheter");
    } catch (feil) {
      alert(feil.message);
      return;
    }

    rader.replaceChildren();
    nyheter.forEach((n) => {
      const rad = document.createElement("tr");

      const rediger = lagKnapp("Rediger", n.tittel);
      rediger.addEventListener("click", () => apneDialog(dialog, "Rediger nyhet", tilSkjema(n)));

      const slett = lagSlettKnapp(n.tittel, async () => {
        try {
          await api("DELETE", "/api/admin/nyheter/" + n.id);
        } catch (feil) {
          alert(feil.message);
        }
        await visNyheter();
      });

      const tittel = lagCelle(n.tittel, "th");
      tittel.scope = "row";
      rad.append(
        lagCelle(lagMiniBilde({ bilde: n.bildesti })),
        tittel,
        lagCelle(n.dato ? lagDato(n.dato) : "Kladd"),
        lagCelle(kortnavnAdmin(n.lokallag)),
        lagHandlinger(rediger, slett)
      );
      rader.append(rad);
    });

    tom.hidden = nyheter.length > 0;
  }

  function kortnavnAdmin(navn) {
    return (navn || "").replace(" Husflidslag", "");
  }

  document.getElementById("ny-nyhet").addEventListener("click", () =>
    apneDialog(dialog, "Ny nyhet", { dato: new Date().toISOString().slice(0, 10) })
  );

  kobleBilde(dialog);

  kobleDialog(dialog, async (data) => {
    const id = Number(data.id);
    const nyhet = {
      tittel: data.tittel.trim(),
      ingress: data.ingress.trim(),
      innhold: data.tekst.trim(),
      bildesti: data.bilde || "",
      bildealt: (data.bildeAlt || "").trim(),
      lokallagId: data.lokallag === "fylket" ? null : Number(data.lokallag),
      status: "PUBLISERT",
      dato: data.dato
    };

    try {
      if (id) await api("PUT", "/api/admin/nyheter/" + id, nyhet);
      else await api("POST", "/api/admin/nyheter", nyhet);
    } catch (feil) {
      return feil.message;
    }
    await visNyheter();
  });

  fyllLokallag().then(visNyheter);
}

if (adminSide === "pameldinger") {
  const rader = document.getElementById("pamelding-rader");
  const tom = document.getElementById("pamelding-tom");
  const antall = document.getElementById("pamelding-antall");
  const filter = document.getElementById("filter-kurs");
  const melding = document.getElementById("pamelding-melding");
  let kurs = [];

  function visMelding(tekst) {
    melding.textContent = tekst;
    melding.hidden = false;
  }

  async function hentKurs() {
    try {
      kurs = await api("GET", "/api/admin/kurs");
    } catch (feil) {
      visMelding(feil.message);
      return;
    }
    const valgtKurs = filter.value || new URLSearchParams(location.search).get("kurs");
    filter.replaceChildren(new Option("Alle kurs", ""));
    velgKurs.replaceChildren(new Option("Velg kurs", ""));
    kurs.forEach((k) => {
      filter.add(new Option(k.tittel, k.id));
      if (k.status === "PUBLISERT") velgKurs.add(new Option(k.tittel, k.id));
    });
    if (valgtKurs) filter.value = valgtKurs;
  }

  async function visPameldinger() {
    const kursId = filter.value;
    let pameldinger;
    try {
      pameldinger = await api("GET", "/api/admin/pameldinger" + (kursId ? "?kurs=" + kursId : ""));
    } catch (feil) {
      visMelding(feil.message);
      return;
    }

    rader.replaceChildren();
    pameldinger.forEach((p) => {
      const rad = document.createElement("tr");

      const kontakt = document.createElement("td");
      kontakt.className = "admin-kontakt";
      const epost = document.createElement("a");
      epost.href = "mailto:" + p.epost;
      epost.textContent = p.epost;
      kontakt.append(epost);
      if (p.telefon) {
        const telefon = document.createElement("a");
        telefon.href = "tel:" + p.telefon.replace(/\s/g, "");
        telefon.textContent = p.telefon;
        kontakt.append(telefon);
      }

      const status = document.createElement("span");
      status.className = "plasser " + (p.status === "venteliste" ? "plasser-faa" : "plasser-ledig");
      status.textContent = p.status === "venteliste" ? "Venteliste" : "Påmeldt";

      const knapper = [];
      if (p.status === "venteliste") {
        const giPlass = lagKnapp("Gi plass", p.navn);
        giPlass.addEventListener("click", async () => {
          try {
            await api("POST", "/api/admin/pameldinger/" + encodeURIComponent(p.referanse) + "/gi-plass");
            visMelding(p.navn + " har fått plass på " + p.kursTittel + ".");
          } catch (feil) {
            visMelding(feil.message);
          }
          await oppdater();
        });
        knapper.push(giPlass);
      }
      knapper.push(lagSlettKnapp(p.navn, async () => {
        try {
          await api("DELETE", "/api/admin/pameldinger/" + encodeURIComponent(p.referanse));
          visMelding(p.navn + " er slettet fra " + p.kursTittel + ".");
        } catch (feil) {
          visMelding(feil.message);
        }
        await oppdater();
      }));

      rad.append(lagCelle(p.navn, "th"), kontakt, lagCelle(p.kursTittel), lagCelle(p.medlem ? "Ja" : "Nei"), lagCelle(status), lagHandlinger(...knapper));
      rad.firstChild.scope = "row";
      rader.append(rad);
    });

    antall.textContent = "(" + pameldinger.length + ")";
    tom.hidden = pameldinger.length > 0;
  }

  async function oppdater() {
    await hentKurs();
    await visPameldinger();
  }

  filter.addEventListener("change", visPameldinger);

  const dialog = document.getElementById("pamelding-dialog");
  const velgKurs = document.getElementById("pam-kurs");
  const plasserTekst = document.getElementById("pam-plasser");

  function visPlasser() {
    const k = kurs.find((rad) => rad.id === Number(velgKurs.value));
    if (!k) {
      plasserTekst.textContent = "";
      return;
    }
    plasserTekst.textContent = k.ledigePlasser > 0
      ? k.ledigePlasser + " av " + k.kapasitet + " plasser ledige."
      : "Kurset er fullt. Personen blir satt på venteliste.";
  }
  velgKurs.addEventListener("change", visPlasser);

  document.getElementById("ny-pamelding").addEventListener("click", () => {
    apneDialog(dialog, "Legg til påmelding", { kursId: filter.value });
    visPlasser();
  });

  kobleDialog(dialog, async (data) => {
    const kursId = Number(data.kursId);
    const navn = data.fornavn.trim() + " " + data.etternavn.trim();
    let kvittering;
    try {
      kvittering = await api("POST", "/api/admin/kurs/" + kursId + "/pameldinger", {
        nokkel: crypto.randomUUID(),
        fornavn: data.fornavn.trim(),
        etternavn: data.etternavn.trim(),
        epost: data.epost.trim().toLowerCase(),
        telefon: data.telefon.trim(),
        fodselsar: Number.parseInt(data.fodselsar, 10),
        medlem: data.medlem,
        medlemsnummer: "",
        kommentar: (data.kommentar || "").trim(),
        samtykke: data.samtykke === "on"
      });
    } catch (feil) {
      return feil.message;
    }

    filter.value = String(kursId);
    await oppdater();
    visMelding(kvittering.status === "venteliste"
      ? kvittering.kursTittel + " er fullt. " + navn + " er satt på venteliste."
      : navn + " er påmeldt " + kvittering.kursTittel + ".");
  });

  oppdater();
}
