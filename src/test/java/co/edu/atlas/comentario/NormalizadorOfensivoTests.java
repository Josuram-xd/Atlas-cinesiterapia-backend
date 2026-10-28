package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.atlas.comentario.filtro.NormalizadorOfensivo;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NormalizadorOfensivoTests {

    static final Set<String> LISTA = Set.of("puta", "mierda", "estupido", "hijo de puta", "cabron", "idiota");

    private static boolean ofende(String texto) {
        return NormalizadorOfensivo.contieneOfensiva(texto, LISTA);
    }

    @Test
    void normalizesCaseAndAccents() {
        assertThat(NormalizadorOfensivo.normalizarPalabra("  ESTÚPIDO ")).isEqualTo("estupido");
        assertThat(NormalizadorOfensivo.normalizarPalabra("Cabrón")).isEqualTo("cabron");
    }

    @Test
    void normalizesSubstitutionsInListEntries() {
        assertThat(NormalizadorOfensivo.normalizarPalabra("m13rd4")).isEqualTo("mierda");
        assertThat(NormalizadorOfensivo.normalizarPalabra("hijo   de-puta")).isEqualTo("hijo de puta");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "eres una puta",
        "ERES UNA PUTA",
        "qué estúpido eres",
        "ESTUPIDO",
        "esto es una m13rd4",
        "p4ta no, pero m1erd4 si",
        "eres 1d10t4",
        "p.u.t.a",
        "p-u-t-a",
        "p u t a",
        "eres p_u_t_a de verdad",
        "pu.ta",
        "puuuuta",
        "mierdaaaa",
        "que mierda!!",
        "hijo-de-puta",
        "hijo de   puta",
        "m​ierda",
        "es un idiota, jaja"
    })
    void detectsOffensiveVariants(String texto) {
        assertThat(ofende(texto)).as(texto).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Excelente ejercicio, muy claro",
        "Hacer 3 series de 10 repeticiones",
        "la reputación del método",
        "computadora y disputa",
        "flexión de rodilla a 90 grados",
        "una a una, y a y e",
        "",
        "   "
    })
    void doesNotFlagCleanTextOrWordsContainingOffensiveSubstrings(String texto) {
        assertThat(ofende(texto)).as(texto).isFalse();
    }

    @Test
    void emptyListNeverFlags() {
        assertThat(NormalizadorOfensivo.contieneOfensiva("puta", Set.of())).isFalse();
    }

    @Test
    void nullTextIsNotOffensive() {
        assertThat(NormalizadorOfensivo.contieneOfensiva(null, LISTA)).isFalse();
    }
}
