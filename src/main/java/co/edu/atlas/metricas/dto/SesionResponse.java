package co.edu.atlas.metricas.dto;

import co.edu.atlas.metricas.SesionUso;
import java.time.Instant;

public record SesionResponse(
        Long sesionId, boolean nuevaSesion, Instant inicio, Instant ultimoPing, long duracionSegundos) {

    public static SesionResponse de(SesionUso s, boolean nueva) {
        return new SesionResponse(s.getId(), nueva, s.getInicioAt(), s.getUltimoPingAt(), s.getDuracionSegundos());
    }
}
