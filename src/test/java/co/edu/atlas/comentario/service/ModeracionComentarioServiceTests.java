package co.edu.atlas.comentario.service;

import static co.edu.atlas.support.Entidades.comentario;
import static co.edu.atlas.support.Entidades.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.ComentarioRepository;
import co.edu.atlas.comentario.EstadoComentario;
import co.edu.atlas.comentario.ReporteComentarioRepository;
import co.edu.atlas.comentario.TipoRevision;
import co.edu.atlas.comentario.dto.ComentarioRevisionResponse;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ModeracionComentarioServiceTests {

    @Mock ComentarioRepository comentarios;
    @Mock ReporteComentarioRepository reportes;
    @Mock EjercicioRepository ejercicios;

    MutableClock clock = new MutableClock();

    private ModeracionComentarioService servicio() {
        return new ModeracionComentarioService(comentarios, reportes, ejercicios, clock);
    }

    @Test
    void colaIncluyeNombreDeFichaYTotalDeReportes() {
        Comentario c = comentario(10L, 5L, usuario(1L), null, EstadoComentario.EN_REVISION);
        Ejercicio ficha = mock(Ejercicio.class);
        when(ficha.getId()).thenReturn(5L);
        when(ficha.getNombre()).thenReturn("Puente");
        when(comentarios.colaRevision(eq(true), eq(true), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(c)));
        when(reportes.contarPorComentario(anyCollection())).thenReturn(List.<Object[]>of(new Object[] {10L, 3L}));
        when(ejercicios.findAllById(anyIterable())).thenReturn(List.of(ficha));

        ComentarioRevisionResponse r = servicio().cola(null, 0, 20).content().get(0);

        assertThat(r.ejercicio().nombre()).isEqualTo("Puente");
        assertThat(r.totalReportes()).isEqualTo(3);
        assertThat(r.autor().correo()).isEqualTo("u1@correo.com");
    }

    @Test
    void colaOcultadosNoIncluyeReportados() {
        when(comentarios.colaRevision(eq(true), eq(false), any(Pageable.class))).thenReturn(Page.empty());

        Pagina<ComentarioRevisionResponse> cola = servicio().cola(TipoRevision.OCULTADOS, 0, 20);

        assertThat(cola.content()).isEmpty();
        verify(reportes, never()).contarPorComentario(anyCollection());
    }

    @Test
    void aceptarPublicaElComentarioYLimpiaSusReportes() {
        Comentario c = comentario(10L, 5L, usuario(1L), null, EstadoComentario.EN_REVISION);
        when(comentarios.findById(10L)).thenReturn(Optional.of(c));

        ComentarioRevisionResponse r = servicio().aceptar(10L);

        assertThat(c.getEstado()).isEqualTo(EstadoComentario.PUBLICADO);
        assertThat(r.totalReportes()).isZero();
        verify(reportes).borrarDeComentario(10L);
    }

    @Test
    void aceptarUnComentarioEliminadoOInexistenteFallaComoNoEncontrado() {
        Comentario eliminado = comentario(11L, 5L, usuario(1L), null, EstadoComentario.PUBLICADO);
        eliminado.eliminar(clock.instant());
        when(comentarios.findById(11L)).thenReturn(Optional.of(eliminado));
        when(comentarios.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().aceptar(11L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("COMENTARIO_NO_ENCONTRADO"));
        assertThatThrownBy(() -> servicio().aceptar(99L)).isInstanceOf(ApiException.class);
        verify(reportes, never()).borrarDeComentario(any());
    }

    @Test
    void eliminarComoAdminBorraBlandoYDescartaReportes() {
        Comentario c = comentario(10L, 5L, usuario(1L), null, EstadoComentario.PUBLICADO);
        when(comentarios.findById(10L)).thenReturn(Optional.of(c));

        servicio().eliminar(10L);

        assertThat(c.getEstado()).isEqualTo(EstadoComentario.ELIMINADO);
        assertThat(c.getDeletedAt()).isEqualTo(clock.instant());
        verify(reportes).borrarDeComentario(10L);
    }
}
