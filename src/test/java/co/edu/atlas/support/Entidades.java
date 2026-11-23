package co.edu.atlas.support;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.EstadoComentario;
import org.springframework.test.util.ReflectionTestUtils;

/** Entidades con id para pruebas unitarias: el id real lo asigna la base de datos. */
public final class Entidades {

    private Entidades() {}

    public static <T> T conId(T entidad, long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }

    public static Usuario usuario(long id) {
        return conId(new Usuario("u" + id + "@correo.com", "HASH", RolPerfil.ESTUDIANTE, RolSistema.USER), id);
    }

    public static Comentario comentario(long id, long ejercicioId, Usuario autor, Long padreId, EstadoComentario estado) {
        return conId(new Comentario(ejercicioId, autor, padreId, "texto", estado), id);
    }
}
