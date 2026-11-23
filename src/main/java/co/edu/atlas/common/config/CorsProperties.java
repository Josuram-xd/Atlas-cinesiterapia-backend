package co.edu.atlas.common.config;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origenes permitidos, tomados de la variable de entorno CORS_ORIGINS (separados por coma). Cada origen debe ser
 * exacto ({@code esquema://host[:puerto]}): se rechazan comodines, rutas y barras finales para que un error de
 * configuracion falle al arrancar en vez de abrir la API a cualquier sitio.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    private static final Pattern ORIGEN_EXACTO = Pattern.compile("^https?://[A-Za-z0-9.-]+(:\\d{1,5})?$");

    public CorsProperties {
        allowedOrigins = allowedOrigins == null
                ? List.of()
                : allowedOrigins.stream()
                        .map(String::trim)
                        .filter(o -> !o.isEmpty())
                        .peek(CorsProperties::validar)
                        .toList();
    }

    private static void validar(String origen) {
        if (!ORIGEN_EXACTO.matcher(origen).matches()) {
            throw new IllegalStateException("CORS_ORIGINS invalido: '" + origen
                    + "'. Usa origenes exactos como https://atlas.ejemplo.com (sin comodines, rutas ni barra final)");
        }
    }
}
