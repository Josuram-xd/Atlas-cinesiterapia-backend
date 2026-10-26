package co.edu.atlas.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Origenes permitidos, tomados de la variable de entorno CORS_ORIGINS (separados por coma). */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
