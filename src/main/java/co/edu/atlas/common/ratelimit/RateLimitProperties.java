package co.edu.atlas.common.ratelimit;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Limite simple por cliente y ruta: maximo {@code maxRequests} peticiones por ventana. */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @DefaultValue("10") int maxRequests, @DefaultValue("1m") Duration window) {}
