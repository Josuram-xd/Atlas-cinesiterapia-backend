package co.edu.atlas.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.atlas.common.error.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReglasMediaTests {

    @ParameterizedTest
    @ValueSource(strings = {"video/mp4", "video/webm", "image/gif"})
    void acceptsSupportedVideoFormats(String tipo) {
        assertThatCode(() -> ReglasMedia.validar(MediaTipo.VIDEO, tipo, 1024)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png", "image/webp"})
    void acceptsSupportedImageFormats(String tipo) {
        assertThatCode(() -> ReglasMedia.validar(MediaTipo.IMAGEN, tipo, 1024)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "video/quicktime", "text/html", "image/svg+xml", ""})
    void rejectsOtherFormatsForVideo(String tipo) {
        assertThatThrownBy(() -> ReglasMedia.validar(MediaTipo.VIDEO, tipo, 1024))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_TIPO_NO_SOPORTADO"));
    }

    @Test
    void gifIsNotAcceptedAsImageAndSvgNeitherBecauseOfScripts() {
        assertThatThrownBy(() -> ReglasMedia.validar(MediaTipo.IMAGEN, "image/gif", 1024))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> ReglasMedia.validar(MediaTipo.IMAGEN, "image/svg+xml", 1024))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void contentTypeComparisonIgnoresCaseAndParameters() {
        assertThatCode(() -> ReglasMedia.validar(MediaTipo.IMAGEN, "IMAGE/PNG", 10)).doesNotThrowAnyException();
    }

    @Test
    void enforcesSizeLimitsAtTheBoundary() {
        assertThatCode(() -> ReglasMedia.validar(MediaTipo.VIDEO, "video/mp4", 26_214_400L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> ReglasMedia.validar(MediaTipo.VIDEO, "video/mp4", 26_214_401L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_DEMASIADO_GRANDE"));
        assertThatCode(() -> ReglasMedia.validar(MediaTipo.IMAGEN, "image/png", 5_242_880L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> ReglasMedia.validar(MediaTipo.IMAGEN, "image/png", 5_242_881L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_DEMASIADO_GRANDE"));
    }

    @Test
    void rejectsEmptyFiles() {
        assertThatThrownBy(() -> ReglasMedia.validar(MediaTipo.IMAGEN, "image/png", 0)).isInstanceOf(ApiException.class);
    }

    @Test
    void mapsExtensionsAndLimits() {
        assertThat(ReglasMedia.extension("image/jpeg")).isEqualTo("jpg");
        assertThat(ReglasMedia.extension("video/webm")).isEqualTo("webm");
        assertThat(ReglasMedia.limite(MediaTipo.VIDEO)).isEqualTo(1);
        assertThat(ReglasMedia.limite(MediaTipo.IMAGEN)).isEqualTo(8);
    }
}
