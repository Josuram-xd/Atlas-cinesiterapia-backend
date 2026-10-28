package co.edu.atlas.metricas;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Ajustes de metricas (prefijo app.metricas). {@code inactividad}: sin ping durante este tiempo el siguiente ping
 * abre una sesion nueva. {@code ventanaDuplicados}: visitas repetidas del mismo usuario al mismo destino dentro de
 * esta ventana se ignoran. {@code maxVisitasPorMinuto}: tope de visitas por usuario y minuto (429 al superarlo). {@code intervaloMinimoPing}: pings mas seguidos que esto no escriben en la base.
 */
@ConfigurationProperties(prefix = "app.metricas")
public record MetricasProperties(
        @DefaultValue("30m") Duration inactividad,
        @DefaultValue("10s") Duration ventanaDuplicados,
        @DefaultValue("5s") Duration intervaloMinimoPing,
        @DefaultValue("30") int maxVisitasPorMinuto) {}
