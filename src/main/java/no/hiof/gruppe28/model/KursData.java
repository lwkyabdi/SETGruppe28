package no.hiof.gruppe28.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KursData(
    String tittel,
    String beskrivelse,
    LocalDate dato,
    String tidspunkt,
    String varighet,
    String sted,
    Long lokallagId,
    String tema,
    String malgruppe,
    String niva,
    String utstyr,
    BigDecimal medlemspris,
    BigDecimal ordinaerpris,
    int kapasitet,
    String status,
    long kontaktId,
    String bildesti,
    String bildealt
) { }

