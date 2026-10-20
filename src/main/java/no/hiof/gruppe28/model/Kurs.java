package no.hiof.gruppe28.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Kurs(
        long id, String tittel, String beskrivelse, LocalDate dato,
        String tidspunkt, String varighet, String sted, String lokallag,
        String tema, String malgruppe, String niva, String utstyr,
        BigDecimal medlemspris, BigDecimal ordinaerpris,
        int kapasitet, int ledigePlasser, Kontaktperson kontakt) {}
