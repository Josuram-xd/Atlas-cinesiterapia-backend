package co.edu.atlas.comentario.dto;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.Usuario;

/** Autor visible para otros usuarios: nunca incluye el correo completo (privacidad). */
public record AutorRef(Long id, String nombre, RolPerfil rolPerfil) {

    public static AutorRef de(Usuario u) {
        String correo = u.getCorreo();
        int arroba = correo.indexOf('@');
        return new AutorRef(u.getId(), arroba > 0 ? correo.substring(0, arroba) : correo, u.getRolPerfil());
    }
}
