package co.edu.atlas.comentario.filtro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import co.edu.atlas.comentario.EstadoComentario;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FiltroOfensivoServiceTests {

    @Mock
    PalabraOfensivaRepository palabras;

    @InjectMocks
    FiltroOfensivoService filtro;

    @Test
    void detectaLaPalabraSinImportarMayusculasYMarcaRevision() {
        when(palabras.todasNormalizadas()).thenReturn(List.of("tonto"));

        assertThat(filtro.esOfensivo("Eres un TONTO")).isTrue();
        assertThat(filtro.estadoPara("eres tonto")).isEqualTo(EstadoComentario.EN_REVISION);
    }

    @Test
    void textoLimpioSePublica() {
        when(palabras.todasNormalizadas()).thenReturn(List.of("tonto"));

        assertThat(filtro.esOfensivo("buen ejercicio de cadera")).isFalse();
        assertThat(filtro.estadoPara("buen ejercicio de cadera")).isEqualTo(EstadoComentario.PUBLICADO);
    }
}
