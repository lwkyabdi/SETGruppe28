"use strict";
const { createApp } = Vue;
const page = document.body.dataset.page;
const query = new URLSearchParams(window.location.search);
const slug = value => String(value ?? "").toLowerCase().replaceAll("ø", "o").replaceAll("æ", "ae")
  .normalize("NFD").replace(/[\u0300-\u036f]/g, "").replaceAll(" ", "-");
const dato = value => value ? new Intl.DateTimeFormat("nb-NO", { dateStyle: "long" })
  .format(new Date(value + "T12:00:00")) : "Dato kommer";
const pris = value => new Intl.NumberFormat("nb-NO", { style: "currency", currency: "NOK", maximumFractionDigits: 0 }).format(value);

const news = [
  { id: 1, tittel: "Høstprogrammet er klart", dato: "2026-10-15", lokallag: "Østfold",
    ingress: "Se eksempler på høstens kurstilbud.", tekst: "Dette er en eksempelnyhet til studentdemoen. Se kurssidene for demonstrasjon av kursoversikt og detaljer." },
  { id: 2, tittel: "Bunadssøm for nybegynnere", dato: "2026-10-12", lokallag: "Fredrikstad",
    ingress: "En introduksjon til søm og håndverkstradisjoner.", tekst: "Prøv håndsøm og bli kjent med ulike materialer. Se bunadskurset i oversikten for et eksempel på kursinformasjon." },
  { id: 3, tittel: "Velkommen til håndverksfellesskapet", dato: "2026-10-10", lokallag: "Moss",
    ingress: "Utforsk strikking, veving og andre håndverk.", tekst: "Alle nyheter, kurs og kontaktopplysninger i denne studentdemoen er eksempeldata." }
];

const Header = {
  props: ["active"],
  data: () => ({ open: false }),
  template: `<header class="topp">
    <a href="/index.html" class="logo"><strong>Østfold</strong><span>Husflidslag</span></a>
    <nav class="hovedmeny" aria-label="Hovedmeny"><ul>
      <li><a href="/index.html" :aria-current="active === 'hjem' ? 'page' : null">Hjem</a></li>
      <li><a href="/kurs.html" :aria-current="['kurs','detalj'].includes(active) ? 'page' : null">Kurs og aktiviteter</a></li>
      <li><a href="/aktuelt.html" :aria-current="['aktuelt','nyhet'].includes(active) ? 'page' : null">Aktuelt</a></li>
      <li><a href="/kontakt.html" :aria-current="active === 'kontakt' ? 'page' : null">Kontakt oss</a></li>
      <li><a href="/admin.html">Admin</a></li>
    </ul></nav>
    <nav class="header-hoyre" aria-label="Finn kurs"><a href="/kurs.html" class="knapp knapp-gronn">Finn kurs</a></nav>
    <button type="button" class="menyknapp" :aria-expanded="open" aria-controls="mobilmeny"
      :aria-label="open ? 'Lukk meny' : 'Åpne meny'" @click="open = !open">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M3 6h18M3 12h18M3 18h18"></path></svg>
    </button>
    <nav id="mobilmeny" class="mobilmeny" aria-label="Mobilmeny" v-show="open"><ul>
      <li><a href="/index.html">Hjem</a></li><li><a href="/kurs.html">Kurs og aktiviteter</a></li>
      <li><a href="/aktuelt.html">Aktuelt</a></li><li><a href="/kontakt.html">Kontakt oss</a></li>
      <li><a href="/admin.html">Admin</a></li>
    </ul></nav>
  </header>`
};
const Footer = {
  template: `<footer class="bunn">
    <section><h2>Østfold Husflidslag</h2><p>Studentdemo med kurs, aktiviteter og informasjon.</p></section>
    <nav aria-label="Snarveier"><h2>Snarveier</h2><ul><li><a href="/kurs.html">Kurs og aktiviteter</a></li><li><a href="/aktuelt.html">Aktuelt</a></li><li><a href="/kontakt.html">Kontakt oss</a></li></ul></nav>
    <section><h2>Spørsmål om kurs?</h2><p>Du finner kontaktpersonen på hver kursside.</p><a href="/kontakt.html">Se kurskontakter</a></section>
    <p class="copyright"><small>© 2026 Østfold Husflidslag · Studentdemo</small></p>
  </footer>`
};
const KursKort = {
  props: ["kurs"],
  methods: { dato, pris },
  template: `<article class="kort kurs">
    <figure><img src="/bilder/kurs-placeholder.svg" alt=""></figure>
    <p class="kurs-tema">{{ kurs.tema }}</p><p class="kurs-lokallag">{{ kurs.lokallag }} Husflidslag</p>
    <h3>{{ kurs.tittel }}</h3>
    <ul class="kurs-info"><li class="dato"><time :datetime="kurs.dato">{{ dato(kurs.dato) }}</time></li><li class="tid">{{ kurs.tidspunkt }} · {{ kurs.varighet }}</li><li>{{ kurs.sted }}</li></ul>
    <footer><p class="plasser" :class="kurs.ledigePlasser === 0 ? 'plasser-fullt' : 'plasser-ledig'">{{ kurs.ledigePlasser === 0 ? 'Fullt' : kurs.ledigePlasser + ' ledige plasser' }}</p><p class="pris"><strong>{{ pris(kurs.medlemspris) }}</strong> for medlem</p></footer>
    <a :href="'/kurs-detalj.html?id=' + kurs.id" class="knapp knapp-gronn">Se kurset<span class="skjult">: {{ kurs.tittel }}</span></a>
  </article>`
};

createApp({
  components: { "site-header": Header, "site-footer": Footer, "kurs-kort": KursKort },
  data() {
    return { news, courses: [], course: null, loading: false, error: "",
      search: query.get("sok") || "", theme: query.get("tema") || "", localGroup: query.get("lokallag") || "" };
  },
  computed: {
    filtered() {
      return this.courses.filter(k =>
        (k.tittel + " " + k.beskrivelse + " " + k.sted).toLowerCase().includes(this.search.trim().toLowerCase())
        && (!this.theme || slug(k.tema) === this.theme)
        && (!this.localGroup || slug(k.lokallag) === this.localGroup));
    },
    themes() { return [...new Set(this.courses.map(k => k.tema))].sort(); },
    localGroups() { return [...new Set(this.courses.map(k => k.lokallag))].sort(); },
    contacts() { return [...new Map(this.courses.map(k => [k.kontakt.id, k.kontakt])).values()]; },
    currentNews() { return this.news.find(n => n.id === Number(query.get("id"))); }
  },
  methods: {
    dato, pris, slug,
    updateAddress() {
      const q = new URLSearchParams();
      if (this.search) q.set("sok", this.search);
      if (this.theme) q.set("tema", this.theme);
      if (this.localGroup) q.set("lokallag", this.localGroup);
      window.history.replaceState(null, "", window.location.pathname + (q.size ? "?" + q : ""));
    },
    async load() {
      if (!["hjem", "kurs", "detalj", "kontakt"].includes(page)) return;
      this.loading = true; this.error = ""; this.course = null;
      try {
        let url = page === "hjem" ? "/api/kurs/kommende" : "/api/kurs";
        if (page === "detalj") {
          const id = query.get("id");
          if (!id || !/^[1-9]\d*$/.test(id)) throw new Error("Velg et gyldig kurs fra kursoversikten.");
          url += "/" + encodeURIComponent(id);
        }
        const response = await fetch(url, { headers: { Accept: "application/json" } });
        if (!response.ok) {
          const body = await response.json().catch(() => null);
          throw new Error(body?.melding || "Kurs kan ikke hentes akkurat nå.");
        }
        const data = await response.json();
        if (page === "detalj") this.course = data;
        else {
          if (!Array.isArray(data)) throw new Error("Serveren returnerte et uventet svar.");
          this.courses = data;
        }
      } catch (e) {
        this.error = e.message || "Kurs kan ikke hentes akkurat nå.";
      } finally { this.loading = false; }
    }
  },
  mounted() { this.load(); }
}).mount("#app");
