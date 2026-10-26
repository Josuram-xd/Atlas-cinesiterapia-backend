package co.edu.atlas.auth.dto;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;

public record UsuarioResponse(Long id, String correo, RolPerfil rolPerfil, RolSistema rolSistema) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getCorreo(), u.getRolPerfil(), u.getRolSistema());
    }
}
