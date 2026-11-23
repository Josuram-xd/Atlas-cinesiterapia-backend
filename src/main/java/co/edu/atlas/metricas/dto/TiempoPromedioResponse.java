package co.edu.atlas.metricas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

/**
 * Tiempo promedio por sesion. {@code promedioSegundos} es null si no hay sesiones con duracion registrada
 * (mostrar "no hay datos suficientes"). {@code variacionPorcentaje} solo existe si ambos periodos tienen datos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TiempoPromedioResponse(
        LocalDate desde,
        LocalDate hasta,
        long sesiones,
        Long promedioSegundos,
        Anterior anterior,
        Double variacionPorcentaje) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Anterior(LocalDate desde, LocalDate hasta, long sesiones, Long promedioSegundos) {}
}
