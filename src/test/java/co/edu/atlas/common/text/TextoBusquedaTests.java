package co.edu.atlas.common.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextoBusquedaTests {

    @Test
    void removesAccentsAndLowercases() {
        assertThat(TextoBusqueda.normalizar("Flexión de Rodilla")).isEqualTo("flexion de rodilla");
    }

    @Test
    void collapsesAndTrimsWhitespace() {
        assertThat(TextoBusqueda.normalizar("  Sentadilla   profunda ")).isEqualTo("sentadilla profunda");
    }

    @Test
    void treatsEnyeAsPlainN() {
        assertThat(TextoBusqueda.normalizar("Muñeca")).isEqualTo("muneca");
    }

    @Test
    void nullBecomesEmpty() {
        assertThat(TextoBusqueda.normalizar(null)).isEmpty();
    }

    @Test
    void escapesLikeWildcards() {
        assertThat(TextoBusqueda.escaparLike("100%_ok\\")).isEqualTo("100\\%\\_ok\\\\");
    }
}
