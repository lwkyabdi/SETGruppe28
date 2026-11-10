package no.hiof.gruppe28.dto;

import java.math.BigDecimal;

public record KursSkjema(
    String tittel,
    String beskrivelse,
    String dato,
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
    Integer kapasitet,
    String status,
    Long kontaktId,
    String bildesti,
    String bildealt
) { }

