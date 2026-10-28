package co.edu.atlas.comentario.service;

import static co.edu.atlas.support.Entidades.comentario;
import static co.edu.atlas.support.Entidades.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.atlas.comentario.ComentarioRepository;
import co.edu.atlas.comentario.EstadoComentario;
import co.edu.atlas.comentario.ReporteComentario;
import co.edu.atlas.comentario.ReporteComentarioRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.ratelimit.RateLimiter;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ReporteComentarioServiceTests {

    @Mock ComentarioRepository comentarios;
    @Mock ReporteComentarioRepository reportes;
    // Limitador real (1 reporte por minuto): en memoria, no hace falta simularlo.
    RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());

    private ReporteComentarioService servicio() {
        return new ReporteComentarioService(comentarios, reportes, limiter);
    }

    @Test
    void reportarComentarioAjenoGuardaElReporte() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 5L, usuario(1L), null, EstadoComentario.PUBLICADO)));
        when(reportes.existsByComentarioIdAndUsuarioId(10L, 2L)).thenReturn(false);
        when(reportes.saveAndFlush(any(ReporteComentario.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio().reportar(10L, 2L).comentarioId()).isEqualTo(10L);
    }

    @Test
    void reportarPorEncimaDelLimiteFallaCon429() {
        limiter.tryAcquire("reportar|2");

        assertThatThrownBy(() -> servicio().reportar(10L, 2L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("DEMASIADOS_INTENTOS"));
        verifyNoInteractions(comentarios, reportes);
    }

    @Test
    void reportarUnComentarioNoVisibleEsNoEncontrado() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 5L, usuario(1L), null, EstadoComentario.EN_REVISION)));

        assertThatThrownBy(() -> servicio().reportar(10L, 2L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("COMENTARIO_NO_ENCONTRADO"));
        verify(reportes, never()).saveAndFlush(any());
    }

    @Test
    void reportarElPropioComentarioEsProhibido() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 5L, usuario(2L), null, EstadoComentario.PUBLICADO)));

        assertThatThrownBy(() -> servicio().reportar(10L, 2L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("REPORTE_PROPIO"));
    }

    @Test
    void reportarDosVecesFallaConYaReportado() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 5L, usuario(1L), null, EstadoComentario.PUBLICADO)));
        when(reportes.existsByComentarioIdAndUsuarioId(10L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> servicio().reportar(10L, 2L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("YA_REPORTADO"));
        verify(reportes, never()).saveAndFlush(any());
    }

    @Test
    void carreraEnElReporteUnicoTambienFallaConYaReportado() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 5L, usuario(1L), null, EstadoComentario.PUBLICADO)));
        when(reportes.existsByComentarioIdAndUsuarioId(10L, 2L)).thenReturn(false);
        when(reportes.saveAndFlush(any(ReporteComentario.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> servicio().reportar(10L, 2L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("YA_REPORTADO"));
    }
}
