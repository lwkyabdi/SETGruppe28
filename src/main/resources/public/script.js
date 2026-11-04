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

//PÅMELDING
//Kjører bare på pamelding.html
const skjema = document.getElementById("pamelding-skjema");

if (skjema) {
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
  if (lagret) {
    ["fornavn", "etternavn", "epost", "telefon", "fodselsar", "medlemsnummer", "kommentar"].forEach(function (navn) {
      skjema.elements[navn].value = lagret[navn] || "";
    });
    const medlemValg = skjema.querySelector('input[name="medlem"][value="' + lagret.medlem + '"]');
    if (medlemValg) medlemValg.checked = true;
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

    //Lagre opplysningene til neste side
    const data = Object.fromEntries(new FormData(skjema));
    data.pris = dinPris.textContent;
    lagrePamelding(data);

    window.location.href = skjema.getAttribute("action");
  });
}

//BEKREFT PÅMELDING
//Kjører bare på bekreft.html
const bekreftSkjema = document.getElementById("bekreft-skjema");

if (bekreftSkjema) {
  const pamelding = hentPamelding();

  if (!pamelding) {
    //Ingen påmelding: vis melding i stedet for tomt kort
    bekreftSkjema.hidden = true;
    document.getElementById("ingen-pamelding").hidden = false;
  } else {
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
  }

  //Bekreft og gå videre til "Du er påmeldt"
  bekreftSkjema.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();

    pamelding.bekreftet = true;
    lagrePamelding(pamelding);

    window.location.href = bekreftSkjema.getAttribute("action");
  });
}

//DU ER PÅMELDT
//Kjører bare på pameldt.html
const pameldt = document.getElementById("pameldt");

if (pameldt) {
  const pamelding = hentPamelding();

  if (pamelding && pamelding.bekreftet) {
    //Vis e-posten som bekreftelsen sendes til
    document.getElementById("pameldt-epost").textContent = pamelding.epost;
  } else {
    //Ingen bekreftet påmelding: vis melding i stedet
    pameldt.hidden = true;
    document.getElementById("ingen-pamelding").hidden = false;
  }
}

//KONTAKTSKJEMA
//Kjører bare på kontakt.html
const kontaktSkjema = document.getElementById("kontakt-skjema");

if (kontaktSkjema) {
  const kontaktFeil = document.getElementById("kontakt-feil");
  const kontaktTakk = document.getElementById("kontakt-takk");

  //Fjern rød ramme når feltet blir riktig
  kontaktSkjema.addEventListener("input", function (hendelse) {
    if (hendelse.target.checkValidity()) {
      hendelse.target.removeAttribute("aria-invalid");
    }
  });

  //Sjekk skjemaet og vis takk
  kontaktSkjema.addEventListener("submit", function (hendelse) {
    hendelse.preventDefault();

    const ugyldige = Array.from(kontaktSkjema.elements).filter(function (felt) {
      return felt.willValidate && !felt.checkValidity();
    });

    kontaktSkjema.querySelectorAll("[aria-invalid]").forEach(function (felt) {
      felt.removeAttribute("aria-invalid");
    });

    if (ugyldige.length > 0) {
      ugyldige.forEach(function (felt) {
        felt.setAttribute("aria-invalid", "true");
      });
      kontaktFeil.textContent = "Fyll ut navn, en gyldig e-post og en melding.";
      kontaktFeil.hidden = false;
      ugyldige[0].focus();
      return;
    }

    //Meldingen sendes ikke noe sted ennå. Det kommer med backend.
    const navn = document.getElementById("kontakt-navn").value.trim();
    const epost = document.getElementById("kontakt-epost").value.trim();

    kontaktTakk.textContent = "Takk, " + navn + "! Vi har mottatt meldingen din og svarer til " + epost + " så snart vi kan.";
    kontaktSkjema.hidden = true;
    kontaktTakk.hidden = false;
    kontaktTakk.focus();
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
