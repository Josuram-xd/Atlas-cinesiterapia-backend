package co.edu.atlas.metricas.dto;

import co.edu.atlas.metricas.Agrupacion;
import java.time.LocalDate;
import java.util.List;

public record VisitasPeriodoResponse(
        LocalDate desde, LocalDate hasta, Agrupacion agrupacion, long total, List<Punto> serie) {

    /** {@code inicio}: primer dia del cubo (el dia, el lunes de la semana o el dia 1 del mes). */
    public record Punto(LocalDate inicio, long visitas) {}
}
