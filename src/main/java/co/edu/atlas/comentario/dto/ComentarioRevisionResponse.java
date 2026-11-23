package co.edu.atlas.comentario.dto;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.EstadoComentario;
import java.time.Instant;

/** Fila de la cola de revision del administrador: incluye el correo del autor (solo visible para admins). */
public record ComentarioRevisionResponse(
        Long id,
        Long padreId,
        EjercicioRef ejercicio,
        AutorAdmin autor,
        String texto,
        EstadoComentario estado,
        boolean ocultadoAutomaticamente,
        long totalReportes,
        Instant createdAt,
        Instant editadoAt) {

    public record EjercicioRef(Long id, String nombre) {}

    public record AutorAdmin(Long id, String correo, RolPerfil rolPerfil) {}

    public static ComentarioRevisionResponse de(Comentario c, String nombreEjercicio, long totalReportes) {
        Usuario a = c.getAutor();
        return new ComentarioRevisionResponse(
                c.getId(),
                c.getPadreId(),
                new EjercicioRef(c.getEjercicioId(), nombreEjercicio),
                new AutorAdmin(a.getId(), a.getCorreo(), a.getRolPerfil()),
                c.getTexto(),
                c.getEstado(),
                c.getEstado() == EstadoComentario.EN_REVISION,
                totalReportes,
                c.getCreatedAt(),
                c.getEditadoAt());
    }
}
