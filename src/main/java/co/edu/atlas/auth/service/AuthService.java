package co.edu.atlas.auth.service;

import co.edu.atlas.auth.dto.LoginRequest;
import co.edu.atlas.auth.dto.TokenResponse;
import co.edu.atlas.auth.dto.UsuarioResponse;
import co.edu.atlas.auth.jwt.JwtProperties;
import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.token.RefreshToken;
import co.edu.atlas.auth.token.RefreshTokenRepository;
import co.edu.atlas.auth.token.Tokens;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** Hash BCrypt valido usado para igualar el tiempo de respuesta cuando el correo no existe. */
    private final String hashFalso;

    private final UsuarioRepository usuarios;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final JwtProperties props;
    private final Clock clock;

    public AuthService(
            UsuarioRepository usuarios,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder encoder,
            JwtService jwt,
            JwtProperties props,
            Clock clock) {
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.props = props;
        this.clock = clock;
        this.hashFalso = encoder.encode("contrasena-inexistente");
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByCorreo(Usuario.normalizar(request.correo())).orElse(null);
        if (!credencialesValidas(usuario, request.password())) {
            throw credencialesInvalidas();
        }
        return emitirTokens(usuario);
    }

    /** Rota el refresh token: el recibido se revoca y se emite uno nuevo. Reutilizar uno revocado cierra todas las sesiones. */
    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse refrescar(String refreshToken) {
        RefreshToken actual = refreshTokens
                .findByTokenHash(Tokens.hash(refreshToken))
                .orElseThrow(AuthService::refreshInvalido);
        Instant ahora = clock.instant();
        if (actual.estaRevocado()) {
            refreshTokens.revocarTodosDe(actual.getUsuarioId(), ahora);
            throw refreshInvalido();
        }
        if (actual.haExpirado(ahora)) {
            throw refreshInvalido();
        }
        Usuario usuario = usuarios.findById(actual.getUsuarioId()).filter(Usuario::isActivo).orElseThrow(AuthService::refreshInvalido);
        actual.revocar(ahora);
        return emitirTokens(usuario);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokens.findByTokenHash(Tokens.hash(refreshToken)).ifPresent(t -> t.revocar(clock.instant()));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> ApiException.unauthorized("NO_AUTENTICADO", "Debes iniciar sesion"));
    }

    private boolean credencialesValidas(Usuario usuario, String password) {
        if (usuario == null) {
            encoder.matches(password, hashFalso);
            return false;
        }
        return usuario.isActivo() && encoder.matches(password, usuario.getPasswordHash());
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        String refreshPlano = Tokens.generar();
        refreshTokens.save(new RefreshToken(
                usuario.getId(), Tokens.hash(refreshPlano), clock.instant().plus(props.refreshTtl())));
        return TokenResponse.bearer(
                jwt.generarAccessToken(usuario), refreshPlano, jwt.accessTtlSegundos(), UsuarioResponse.de(usuario));
    }

    private static ApiException credencialesInvalidas() {
        return ApiException.unauthorized("CREDENCIALES_INVALIDAS", "Correo o contrasena incorrectos");
    }

    private static ApiException refreshInvalido() {
        return ApiException.unauthorized("REFRESH_INVALIDO", "La sesion no es valida, inicia sesion de nuevo");
    }
}
