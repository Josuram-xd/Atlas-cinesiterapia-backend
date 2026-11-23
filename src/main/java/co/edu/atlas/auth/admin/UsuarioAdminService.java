package co.edu.atlas.auth.admin;

import co.edu.atlas.auth.dto.UsuarioResponse;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarios;

    public UsuarioAdminService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Promueve a administrador. Si ya lo era, no cambia nada. */
    @Transactional
    public UsuarioResponse promover(Long usuarioId) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> ApiException.notFound("USUARIO_NO_ENCONTRADO", "El usuario no existe"));
        usuario.setRolSistema(RolSistema.ADMIN);
        return UsuarioResponse.de(usuario);
    }
}
