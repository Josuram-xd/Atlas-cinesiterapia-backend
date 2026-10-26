package co.edu.atlas.auth.service;

import co.edu.atlas.auth.AuthProperties;
import co.edu.atlas.auth.token.PasswordResetToken;
import co.edu.atlas.auth.token.PasswordResetTokenRepository;
import co.edu.atlas.auth.token.RefreshTokenRepository;
import co.edu.atlas.auth.token.Tokens;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.mail.CorreoSender;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository resetTokens;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final CorreoSender correo;
    private final AuthProperties props;
    private final Clock clock;

    public PasswordResetService(
            UsuarioRepository usuarios,
            PasswordResetTokenRepository resetTokens,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder encoder,
            CorreoSender correo,
            AuthProperties props,
            Clock clock) {
        this.usuarios = usuarios;
        this.resetTokens = resetTokens;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.correo = correo;
        this.props = props;
        this.clock = clock;
    }

    /** Si el correo existe envia el enlace; si no, no hace nada. Quien llama responde igual en ambos casos. */
    @Transactional
    public void solicitar(String correoSolicitado) {
        usuarios.findByCorreo(Usuario.normalizar(correoSolicitado)).filter(Usuario::isActivo).ifPresent(this::emitir);
    }

    @Transactional
    public void restablecer(String token, String nuevaPassword) {
        Instant ahora = clock.instant();
        PasswordResetToken guardado = resetTokens
                .findByTokenHash(Tokens.hash(token))
                .filter(t -> t.esUtilizable(ahora))
                .orElseThrow(() -> ApiException.badRequest("TOKEN_INVALIDO", "El enlace no es valido o ya expiro"));
        Usuario usuario = usuarios.findById(guardado.getUsuarioId())
                .orElseThrow(() -> ApiException.badRequest("TOKEN_INVALIDO", "El enlace no es valido o ya expiro"));
        usuario.setPasswordHash(encoder.encode(nuevaPassword));
        guardado.marcarUsado(ahora);
        refreshTokens.revocarTodosDe(usuario.getId(), ahora);
    }

    private void emitir(Usuario usuario) {
        Instant ahora = clock.instant();
        resetTokens.invalidarPendientes(usuario.getId(), ahora);
        String plano = Tokens.generar();
        resetTokens.save(new PasswordResetToken(usuario.getId(), Tokens.hash(plano), ahora.plus(props.resetTtl())));
        enviar(usuario.getCorreo(), plano);
    }

    private void enviar(String destino, String tokenPlano) {
        String enlace = props.frontendUrl() + "/restablecer-password?token=" + tokenPlano;
        String cuerpo = "Recibimos una solicitud para restablecer tu contrasena del Atlas de Cinesiterapia.\n\n"
                + "Usa este enlace (vence en " + props.resetTtl().toMinutes() + " minutos y solo sirve una vez):\n"
                + enlace + "\n\nSi no fuiste tu, ignora este mensaje.";
        try {
            correo.enviar(destino, "Restablecer contrasena", cuerpo);
        } catch (RuntimeException e) {
            log.error("No se pudo enviar el correo de recuperacion", e);
        }
    }
}
