package co.edu.atlas.metricas.service;

import co.edu.atlas.common.error.ApiException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Rango de fechas (UTC, ambos extremos incluidos) validado para las consultas de metricas. */
record Periodo(LocalDate desde, LocalDate hasta) {

    static final long MAX_DIAS = 366;

    static Periodo validar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw ApiException.badRequest("RANGO_INVALIDO", "Indica desde y hasta");
        }
        if (desde.isAfter(hasta)) {
            throw ApiException.badRequest("RANGO_INVALIDO", "desde no puede ser posterior a hasta");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) + 1 > MAX_DIAS) {
            throw ApiException.badRequest("RANGO_INVALIDO", "El rango maximo es de " + MAX_DIAS + " dias");
        }
        return new Periodo(desde, hasta);
    }
}
