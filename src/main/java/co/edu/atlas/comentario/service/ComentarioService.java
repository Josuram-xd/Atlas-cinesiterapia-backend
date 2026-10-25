package co.edu.atlas.comentario.service;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.ComentarioRepository;
import co.edu.atlas.comentario.EstadoComentario;
import co.edu.atlas.comentario.TextoComentario;
import co.edu.atlas.comentario.dto.ComentarioResponse;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.ratelimit.RateLimiter;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
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

    private final ComentarioRepository comentarios;
    private final EjercicioRepository ejercicios;
    private final UsuarioRepository usuarios;
    private final RateLimiter limiter;

    public ComentarioService(
            ComentarioRepository comentarios, EjercicioRepository ejercicios, UsuarioRepository usuarios, RateLimiter limiter) {
        this.comentarios = comentarios;
        this.ejercicios = ejercicios;
        this.usuarios = usuarios;
        this.limiter = limiter;
    }

    public ComentarioResponse crear(Long ejercicioId, String texto, Long actorId) {
        limitar("comentar", actorId);
        exigirFichaPublicada(ejercicioId);
        String limpio = TextoComentario.limpiar(texto);
        Usuario autor = usuarios.getReferenceById(actorId);
        Comentario c = comentarios.save(new Comentario(ejercicioId, autor, null, limpio, EstadoComentario.PUBLICADO));
        return ComentarioResponse.de(c, actorId, List.of());
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

    /** Limite por usuario (ventana deslizante en memoria) contra spam de escritura. */
    void limitar(String accion, Long actorId) {
        if (!limiter.tryAcquire(accion + "|" + actorId)) {
            throw ApiException.tooManyRequests("DEMASIADOS_INTENTOS", "Demasiadas acciones seguidas, espera un momento");
        }
    }

    private void exigirFichaPublicada(Long ejercicioId) {
        ejercicios
                .findById(ejercicioId)
                .filter(e -> e.getEstado() == EstadoEjercicio.PUBLICADO)
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO", "El ejercicio no existe"));
    }
}
