package co.edu.atlas.comentario;

import static co.edu.atlas.support.Entidades.comentario;
import static co.edu.atlas.support.Entidades.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.comentario.dto.ComentarioResponse;
import co.edu.atlas.comentario.filtro.FiltroOfensivoService;
import co.edu.atlas.comentario.service.ComentarioService;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.ratelimit.RateLimiter;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTests {

    @Mock ComentarioRepository comentarios;
    @Mock EjercicioRepository ejercicios;
    @Mock UsuarioRepository usuarios;
    @Mock ReporteComentarioRepository reportes;
    @Mock FiltroOfensivoService filtro;

    MutableClock clock = new MutableClock();
    // Limitador real (1 accion por minuto): en memoria, no hace falta simularlo.
    RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1), clock);

    private ComentarioService servicio() {
        return new ComentarioService(comentarios, ejercicios, usuarios, limiter, reportes, clock, filtro);
    }
    // Limitador real (1 por minuto): en memoria, no hace falta simularlo.
    @Spy
    RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());

    /** Ficha real (no mock): evita stubs anidados dentro de when(...).thenReturn(...). */
    private static Ejercicio ficha(EstadoEjercicio estado) {
        Ejercicio e = new Ejercicio("Ficha", null, null, 1L);
        if (estado == EstadoEjercicio.PUBLICADO) {
            e.publicar(Instant.now());
        }
        return e;
    }

    @Test
    void throttlesUsersWhoCommentTooFast() {
        limiter.tryAcquire("comentar|7");

        assertThatThrownBy(() -> servicio().crear(1L, "hola", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("DEMASIADOS_INTENTOS");
                    assertThat(e.getStatus().value()).isEqualTo(429);
                });
        verify(comentarios, never()).save(any());
    }

    @Test
    void rejectsMissingSheetBeforeSaving() {
        when(ejercicios.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().crear(1L, "hola", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
        verify(comentarios, never()).save(any());
    }

    @Test
    void crearGuardaElComentarioConElEstadoDelFiltro() {
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));
        when(usuarios.getReferenceById(7L)).thenReturn(usuario(7L));
        when(filtro.estadoPara("hola")).thenReturn(EstadoComentario.EN_REVISION);
        when(comentarios.save(any(Comentario.class))).thenAnswer(i -> i.getArgument(0));

        ComentarioResponse r = servicio().crear(1L, "  hola  ", 7L);

        assertThat(r.texto()).isEqualTo("hola");
        assertThat(r.estado()).isEqualTo(EstadoComentario.EN_REVISION);
        assertThat(r.propio()).isTrue();
    }

    @Test
    void crearEnFichaNoPublicadaFalla() {
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));

        assertThatThrownBy(() -> servicio().crear(1L, "hola", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
        verify(comentarios, never()).save(any());
    }

    @Test
    void crearConTextoVacioFallaAntesDeGuardar() {
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));

        assertThatThrownBy(() -> servicio().crear(1L, "   ", 7L)).isInstanceOf(ApiException.class);
        verify(comentarios, never()).save(any());
    }

    @Test
    void responderAUnaRaizPublicadaLaCuelgaDeEsa() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 1L, usuario(1L), null, EstadoComentario.PUBLICADO)));
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));
        when(usuarios.getReferenceById(7L)).thenReturn(usuario(7L));
        when(filtro.estadoPara("gracias")).thenReturn(EstadoComentario.PUBLICADO);
        when(comentarios.save(any(Comentario.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio().responder(10L, "gracias", 7L).padreId()).isEqualTo(10L);
    }

    @Test
    void responderAUnaRespuestaAplanaAlHiloDeLaRaiz() {
        when(comentarios.findById(11L))
                .thenReturn(Optional.of(comentario(11L, 1L, usuario(2L), 10L, EstadoComentario.PUBLICADO)));
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 1L, usuario(1L), null, EstadoComentario.PUBLICADO)));
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));
        when(usuarios.getReferenceById(7L)).thenReturn(usuario(7L));
        when(filtro.estadoPara("ok")).thenReturn(EstadoComentario.PUBLICADO);
        when(comentarios.save(any(Comentario.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio().responder(11L, "ok", 7L).padreId()).isEqualTo(10L);
    }

    @Test
    void responderAUnHiloCuyaRaizEstaEnRevisionFalla() {
        when(comentarios.findById(11L))
                .thenReturn(Optional.of(comentario(11L, 1L, usuario(2L), 10L, EstadoComentario.PUBLICADO)));
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 1L, usuario(1L), null, EstadoComentario.EN_REVISION)));

        assertThatThrownBy(() -> servicio().responder(11L, "ok", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("COMENTARIO_NO_RESPONDIBLE"));
        verify(comentarios, never()).save(any());
    }

    @Test
    void responderAUnComentarioNoVisibleEsNoEncontrado() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 1L, usuario(1L), null, EstadoComentario.EN_REVISION)));

        assertThatThrownBy(() -> servicio().responder(10L, "ok", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("COMENTARIO_NO_ENCONTRADO"));
    }

    @Test
    void editarRecalculaElEstadoConElFiltro() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 1L, usuario(7L), null, EstadoComentario.PUBLICADO)));
        when(filtro.estadoPara("nuevo texto")).thenReturn(EstadoComentario.EN_REVISION);

        ComentarioResponse r = servicio().editar(10L, "nuevo texto", 7L);

        assertThat(r.texto()).isEqualTo("nuevo texto");
        assertThat(r.estado()).isEqualTo(EstadoComentario.EN_REVISION);
        assertThat(r.enRevision()).isTrue();
        assertThat(r.editadoAt()).isEqualTo(clock.instant());
    }

    @Test
    void editarComentarioAjenoEsProhibido() {
        when(comentarios.findById(10L))
                .thenReturn(Optional.of(comentario(10L, 1L, usuario(1L), null, EstadoComentario.PUBLICADO)));

        assertThatThrownBy(() -> servicio().editar(10L, "hola", 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("NO_ES_AUTOR");
                    assertThat(e.getStatus().value()).isEqualTo(403);
                });
        verify(comentarios, never()).save(any());
    }

    @Test
    void eliminarPropioBorraYDescartaReportes() {
        Comentario c = comentario(10L, 1L, usuario(7L), null, EstadoComentario.PUBLICADO);
        when(comentarios.findById(10L)).thenReturn(Optional.of(c));

        servicio().eliminar(10L, 7L);

        assertThat(c.estaEliminado()).isTrue();
        verify(reportes).borrarDeComentario(10L);
    }

    @Test
    void listarAgrupaLasRespuestasVisiblesBajoSuRaiz() {
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));
        when(comentarios.raicesVisibles(eq(1L), eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comentario(10L, 1L, usuario(1L), null, EstadoComentario.PUBLICADO))));
        when(comentarios.respuestasVisibles(anyCollection(), eq(7L)))
                .thenReturn(List.of(comentario(11L, 1L, usuario(2L), 10L, EstadoComentario.PUBLICADO)));

        Pagina<ComentarioResponse> pagina = servicio().listar(1L, 7L, 0, 20);

        assertThat(pagina.content()).hasSize(1);
        assertThat(pagina.content().get(0).respuestas()).extracting(ComentarioResponse::id).containsExactly(11L);
    }

    @Test
    void listarSinComentariosNoBuscaRespuestas() {
        when(ejercicios.findById(1L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));
        when(comentarios.raicesVisibles(eq(1L), eq(7L), any(Pageable.class))).thenReturn(Page.empty());

        assertThat(servicio().listar(1L, 7L, 0, 20).content()).isEmpty();
        verify(comentarios, never()).respuestasVisibles(anyCollection(), any());
    }
}
