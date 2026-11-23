package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.atlas.common.error.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TextoComentarioTests {

    @Test
    void trimsAndKeepsPlainText() {
        assertThat(TextoComentario.limpiar("  Muy útil, gracias  ")).isEqualTo("Muy útil, gracias");
    }

    @Test
    void removesControlAndZeroWidthCharactersButKeepsNewlines() {
        assertThat(TextoComentario.limpiar("ho​la\u0000 mun‮do\nlinea")).isEqualTo("hola mundo\nlinea");
    }

    @Test
    void collapsesExcessiveBlankLines() {
        assertThat(TextoComentario.limpiar("a\n\n\n\n\nb")).isEqualTo("a\n\nb");
    }

    @ParameterizedTest
    @ValueSource(strings = {"<script>alert(1)</script>", "hola <b>x</b>", "</div>", "<!-- x -->", "<img src=x onerror=alert(1)>", "<?php"})
    void rejectsMarkup(String texto) {
        assertThatThrownBy(() -> TextoComentario.limpiar(texto))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("TEXTO_INVALIDO"));
    }

    @Test
    void allowsComparisonSymbols() {
        assertThat(TextoComentario.limpiar("flexión < 90 grados y > 45")).isEqualTo("flexión < 90 grados y > 45");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "​\n\t"})
    void rejectsEmptyAfterCleaning(String texto) {
        assertThatThrownBy(() -> TextoComentario.limpiar(texto))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("TEXTO_INVALIDO"));
    }

    @Test
    void rejectsTooLongText() {
        assertThatThrownBy(() -> TextoComentario.limpiar("a".repeat(1001)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("TEXTO_INVALIDO"));
    }
}
