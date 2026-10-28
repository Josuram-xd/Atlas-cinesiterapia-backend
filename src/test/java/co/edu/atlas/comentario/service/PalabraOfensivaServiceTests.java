package co.edu.atlas.comentario.service;

import static co.edu.atlas.support.Entidades.conId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.comentario.dto.PalabraResponse;
import co.edu.atlas.comentario.filtro.PalabraOfensiva;
import co.edu.atlas.comentario.filtro.PalabraOfensivaRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.web.Pagina;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PalabraOfensivaServiceTests {

    @Mock PalabraOfensivaRepository palabras;

    private PalabraOfensivaService servicio() {
        return new PalabraOfensivaService(palabras);
    }

    @Test
    void crearGuardaLaPalabraSinEspaciosExtremos() {
        when(palabras.findByPalabraNormalizada(anyString())).thenReturn(Optional.empty());
        when(palabras.saveAndFlush(any(PalabraOfensiva.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio().crear("  Tonto ").palabra()).isEqualTo("Tonto");
    }

    @Test
    void crearRechazaPalabrasDeMenosDeTresLetras() {
        assertThatThrownBy(() -> servicio().crear(" ab "))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("PALABRA_INVALIDA"));
        verify(palabras, never()).saveAndFlush(any());
    }

    @Test
    void crearUnaPalabraYaExistenteFallaConDuplicada() {
        when(palabras.findByPalabraNormalizada(anyString()))
                .thenReturn(Optional.of(conId(new PalabraOfensiva("tonto"), 9L)));

        assertThatThrownBy(() -> servicio().crear("Tonto"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("PALABRA_DUPLICADA"));
        verify(palabras, never()).saveAndFlush(any());
    }

    @Test
    void crearQueChocaContraLaBaseFallaConDuplicada() {
        when(palabras.findByPalabraNormalizada(anyString())).thenReturn(Optional.empty());
        when(palabras.saveAndFlush(any(PalabraOfensiva.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> servicio().crear("tonto"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("PALABRA_DUPLICADA"));
    }

    @Test
    void actualizarConLaMismaPalabraPropiaNoEsDuplicada() {
        PalabraOfensiva existente = conId(new PalabraOfensiva("tonto"), 9L);
        when(palabras.findById(9L)).thenReturn(Optional.of(existente));
        when(palabras.findByPalabraNormalizada(anyString())).thenReturn(Optional.of(existente));
        when(palabras.saveAndFlush(any(PalabraOfensiva.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio().actualizar(9L, "TONTO").palabra()).isEqualTo("TONTO");
    }

    @Test
    void actualizarUnaPalabraInexistenteFallaNoEncontrada() {
        when(palabras.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().actualizar(9L, "tonto"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("PALABRA_NO_ENCONTRADA"));
    }

    @Test
    void eliminarBorraLaPalabra() {
        PalabraOfensiva existente = conId(new PalabraOfensiva("tonto"), 9L);
        when(palabras.findById(9L)).thenReturn(Optional.of(existente));

        servicio().eliminar(9L);

        verify(palabras).delete(existente);
    }

    @Test
    void listarBuscaPorTextoNormalizado() {
        when(palabras.findByPalabraNormalizadaContaining(eq("tonto"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new PalabraOfensiva("Tonto"))));

        Pagina<PalabraResponse> pagina = servicio().listar("TONTO", 0, 10);

        assertThat(pagina.content()).extracting(PalabraResponse::palabra).containsExactly("Tonto");
    }

    @Test
    void listarSinTextoNoFiltra() {
        when(palabras.findByPalabraNormalizadaContaining(eq(""), any(Pageable.class))).thenReturn(Page.empty());

        assertThat(servicio().listar(null, 0, 10).content()).isEmpty();
    }
}
