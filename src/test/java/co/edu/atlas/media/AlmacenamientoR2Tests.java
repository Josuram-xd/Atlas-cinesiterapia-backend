package co.edu.atlas.media;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** Firma local de URLs: no abre conexiones ni usa credenciales reales. */
class AlmacenamientoR2Tests {

    private static final Instant AHORA = Instant.parse("2026-01-01T12:00:00Z");

    private AlmacenamientoR2 almacenamiento(String publicBaseUrl) {
        MediaProperties props = new MediaProperties(
                "r2", Duration.ofMinutes(10), Duration.ofHours(1), publicBaseUrl,
                new MediaProperties.R2(
                        "https://cuenta-falsa.r2.cloudflarestorage.com", "AKIAFAKEFAKEFAKEFAKE", "secreto-falso-de-prueba", "atlas-test", "auto"));
        return new AlmacenamientoR2(props, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void presignedUploadUrlTargetsBucketAndBindsContentType() {
        SubidaPrefirmada s = almacenamiento(null).prepararSubida("ejercicios/5/abc.png", "image/png", 1234);

        assertThat(s.metodo()).isEqualTo("PUT");
        assertThat(s.url()).startsWith("https://cuenta-falsa.r2.cloudflarestorage.com/atlas-test/ejercicios/5/abc.png?");
        assertThat(s.url()).contains("X-Amz-Signature=").contains("X-Amz-Expires=600");
        assertThat(s.url().toLowerCase()).contains("content-type");
        assertThat(s.cabeceras()).containsEntry("Content-Type", "image/png");
        assertThat(s.expiraEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(10)));
    }

    @Test
    void readUrlIsSignedWhenNoPublicBaseUrl() {
        String url = almacenamiento(null).urlLectura("ejercicios/5/abc.png");

        assertThat(url).contains("X-Amz-Signature=").contains("X-Amz-Expires=3600");
    }

    @Test
    void readUrlUsesStablePublicBaseWhenConfigured() {
        String url = almacenamiento("https://media.atlas.example/").urlLectura("ejercicios/5/abc.png");

        assertThat(url).isEqualTo("https://media.atlas.example/ejercicios/5/abc.png");
    }
}
