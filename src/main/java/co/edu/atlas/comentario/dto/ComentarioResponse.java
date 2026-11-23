package co.edu.atlas.comentario.dto;

import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.EstadoComentario;
import java.time.Instant;
import java.util.List;

/**
 * Comentario visible para quien consulta. Un comentario ELIMINADO (que conserva respuestas) llega sin texto ni
 * autor. {@code enRevision} solo puede ser true para el propio autor.
 */
public record ComentarioResponse(
        Long id,
        Long ejercicioId,
        Long padreId,
        AutorRef autor,
        String texto,
        EstadoComentario estado,
        boolean enRevision,
        boolean eliminado,
        boolean propio,
        Instant createdAt,
        Instant editadoAt,
        List<ComentarioResponse> respuestas) {

    public static ComentarioResponse de(Comentario c, Long viewerId, List<ComentarioResponse> respuestas) {
        boolean eliminado = c.estaEliminado();
        return new ComentarioResponse(
                c.getId(),
                c.getEjercicioId(),
                c.getPadreId(),
                eliminado ? null : AutorRef.de(c.getAutor()),
                eliminado ? null : c.getTexto(),
                c.getEstado(),
                c.getEstado() == EstadoComentario.EN_REVISION,
                eliminado,
                !eliminado && c.esDe(viewerId),
                c.getCreatedAt(),
                c.getEditadoAt(),
                respuestas);
    }
}
