package co.edu.atlas.auth.admin;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSeedService {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedService.class);
    private static final int MIN_PASSWORD = 8;

    private final AdminProperties props;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public AdminSeedService(AdminProperties props, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.props = props;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    /** Crea el primer administrador si esta configurado y aun no existe. Es idempotente. */
    @Transactional
    public void sembrar() {
        if (!props.configurado()) {
            log.info("Seed de administrador omitido: ADMIN_EMAIL/ADMIN_PASSWORD no configurados");
            return;
        }
        if (props.password().length() < MIN_PASSWORD) {
            log.warn("Seed de administrador omitido: ADMIN_PASSWORD debe tener al menos {} caracteres", MIN_PASSWORD);
            return;
        }
        String correo = Usuario.normalizar(props.email());
        if (usuarios.existsByCorreo(correo)) {
            log.info("Seed de administrador omitido: ya existe una cuenta con ese correo");
            return;
        }
        usuarios.save(new Usuario(correo, encoder.encode(props.password()), RolPerfil.PROFESIONAL, RolSistema.ADMIN));
        log.info("Administrador inicial creado");
    }
}
