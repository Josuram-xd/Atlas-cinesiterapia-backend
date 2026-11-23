package co.edu.atlas.auth.service;

import co.edu.atlas.auth.dto.RegistroRequest;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public RegistroService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String correo = Usuario.normalizar(request.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw correoEnUso();
        }
        String usuarioCorreo = correo.substring(0, correo.indexOf('@'));
        if (usuarioCorreo.length() >= 4 && request.password().toLowerCase(Locale.ROOT).contains(usuarioCorreo)) {
            throw ApiException.badRequest("PASSWORD_DEBIL", "La contrasena no puede contener tu correo");
        }
        Usuario nuevo = new Usuario(correo, encoder.encode(request.password()), request.rolPerfil(), RolSistema.USER);
        try {
            return usuarios.saveAndFlush(nuevo);
        } catch (DataIntegrityViolationException e) {
            throw correoEnUso();
        }
    }

    private ApiException correoEnUso() {
        return ApiException.conflict("CORREO_EN_USO", "Ese correo ya esta en uso");
    }
}
