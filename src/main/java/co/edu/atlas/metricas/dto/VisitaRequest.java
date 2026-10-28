package co.edu.atlas.metricas.dto;

import co.edu.atlas.metricas.TipoVisita;
import jakarta.validation.constraints.NotNull;

public record VisitaRequest(@NotNull(message = "Indica el tipo de visita") TipoVisita tipo, Long ejercicioId) {}
