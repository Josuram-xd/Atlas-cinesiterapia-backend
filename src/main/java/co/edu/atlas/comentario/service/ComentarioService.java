package co.edu.atlas.comentario.service;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.ComentarioRepository;
import co.edu.atlas.comentario.EstadoComentario;
import co.edu.atlas.comentario.ReporteComentarioRepository;
import co.edu.atlas.comentario.TextoComentario;
import co.edu.atlas.comentario.dto.ComentarioResponse;
import co.edu.atlas.comentario.filtro.FiltroOfensivoService;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.ratelimit.RateLimiter;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Escritura y consulta de comentarios de una ficha (HU-21). */
@Service
@Transactional
public class ComentarioService {

    static final String DEMASIADAS_ACCIONES = "Demasiadas acciones seguidas, espera un momento";

    private final ComentarioRepository comentarios;
    private final EjercicioRepository ejercicios;
    private final UsuarioRepository usuarios;
    private final RateLimiter limiter;
    private final ReporteComentarioRepository reportes;
    private final Clock clock;
    private final FiltroOfensivoService filtro;

    public ComentarioService(
            ComentarioRepository comentarios,
            EjercicioRepository ejercicios,
            UsuarioRepository usuarios,
            RateLimiter limiter,
            ReporteComentarioRepository reportes,
            Clock clock,
            FiltroOfensivoService filtro) {
        this.filtro = filtro;
        this.reportes = reportes;
        this.clock = clock;
        this.comentarios = comentarios;
        this.ejercicios = ejercicios;
        this.usuarios = usuarios;
        this.limiter = limiter;
    }

    public ComentarioResponse crear(Long ejercicioId, String texto, Long actorId) {
        limiter.exigir("comentar|" + actorId, DEMASIADAS_ACCIONES);
        exigirFichaPublicada(ejercicioId);
        String limpio = TextoComentario.limpiar(texto);
        Usuario autor = usuarios.getReferenceById(actorId);
        Comentario c = comentarios.save(new Comentario(ejercicioId, autor, null, limpio, filtro.estadoPara(limpio)));
        return ComentarioResponse.de(c, actorId, List.of());
    }

    /**
     * Responde a un comentario (HU-29). Hilos de un nivel: responder a una respuesta se aplana y la nueva respuesta
     * cuelga de la raiz del hilo. El comentario respondido debe ser visible para el usuario y la raiz estar publicada.
     */
    public ComentarioResponse responder(Long comentarioId, String texto, Long actorId) {
        limiter.exigir("comentar|" + actorId, DEMASIADAS_ACCIONES);
        Comentario objetivo = comentarios
                .findById(comentarioId)
                .filter(x -> x.visiblePara(actorId))
                .orElseThrow(() -> ApiException.notFound("COMENTARIO_NO_ENCONTRADO", "El comentario no existe"));
        Comentario raiz = objetivo.esRespuesta()
                ? comentarios
                        .findById(objetivo.getPadreId())
                        .orElseThrow(() -> ApiException.notFound("COMENTARIO_NO_ENCONTRADO", "El comentario no existe"))
                : objetivo;
        if (objetivo.getEstado() != EstadoComentario.PUBLICADO || raiz.getEstado() != EstadoComentario.PUBLICADO) {
            throw ApiException.conflict("COMENTARIO_NO_RESPONDIBLE", "Solo se pueden responder comentarios publicados");
        }
        exigirFichaPublicada(raiz.getEjercicioId());
        String limpio = TextoComentario.limpiar(texto);
        Usuario autor = usuarios.getReferenceById(actorId);
        Comentario r = comentarios.save(
                new Comentario(raiz.getEjercicioId(), autor, raiz.getId(), limpio, filtro.estadoPara(limpio)));
        return ComentarioResponse.de(r, actorId, List.of());
    }

    /** Edita el texto del comentario propio (HU-22). La respuesta no incluye {@code respuestas}. */
    public ComentarioResponse editar(Long id, String texto, Long actorId) {
        Comentario c = propio(id, actorId);
        limiter.exigir("comentar|" + actorId, DEMASIADAS_ACCIONES);
        String limpio = TextoComentario.limpiar(texto);
        c.editar(limpio, filtro.estadoPara(limpio), clock.instant());
        return ComentarioResponse.de(c, actorId, List.of());
    }

    /** Borrado blando del comentario propio (HU-23); sus reportes dejan de tener sentido. */
    public void eliminar(Long id, Long actorId) {
        Comentario c = propio(id, actorId);
        c.eliminar(clock.instant());
        reportes.borrarDeComentario(id);
    }

    /** Comentario que el usuario puede ver y ademas es suyo; ajeno visible = 403, no visible = 404. */
    private Comentario propio(Long id, Long actorId) {
        Comentario c = comentarios
                .findById(id)
                .filter(x -> x.visiblePara(actorId))
                .orElseThrow(() -> ApiException.notFound("COMENTARIO_NO_ENCONTRADO", "El comentario no existe"));
        if (!c.esDe(actorId)) {
            throw ApiException.forbidden("NO_ES_AUTOR", "Solo el autor puede modificar este comentario");
        }
        return c;
    }

    @Transactional(readOnly = true)
    public Pagina<ComentarioResponse> listar(Long ejercicioId, Long viewerId, int page, int size) {
        exigirFichaPublicada(ejercicioId);
        Page<Comentario> raices = comentarios.raicesVisibles(ejercicioId, viewerId, PageRequest.of(page, size));
        List<Long> ids = raices.getContent().stream().map(Comentario::getId).toList();
        Map<Long, List<ComentarioResponse>> respuestas = ids.isEmpty()
                ? Map.of()
                : comentarios.respuestasVisibles(ids, viewerId).stream()
                        .collect(Collectors.groupingBy(
                                Comentario::getPadreId,
                                Collectors.mapping(r -> ComentarioResponse.de(r, viewerId, List.of()), Collectors.toList())));
        return Pagina.de(raices, c -> ComentarioResponse.de(c, viewerId, respuestas.getOrDefault(c.getId(), List.of())));
    }

    private void exigirFichaPublicada(Long ejercicioId) {
        ejercicios
                .findById(ejercicioId)
                .filter(e -> e.getEstado() == EstadoEjercicio.PUBLICADO)
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO", "El ejercicio no existe"));
    }
}
