package co.edu.atlas.media;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracion del almacenamiento de archivos. {@code proveedor} es {@code fake} (desarrollo y tests, sin red)
 * o {@code r2} (Cloudflare R2, con credenciales por variables de entorno).
 */
@ConfigurationProperties(prefix = "app.media")
public record MediaProperties(
        @DefaultValue("fake") String proveedor,
        @DefaultValue("10m") Duration subidaTtl,
        @DefaultValue("1h") Duration lecturaTtl,
        String publicBaseUrl,
        @DefaultValue R2 r2) {

    public record R2(String endpoint, String accessKey, String secretKey, String bucket, @DefaultValue("auto") String region) {}
}
