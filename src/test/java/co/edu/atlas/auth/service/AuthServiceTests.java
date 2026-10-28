package co.edu.atlas.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.AuthProperties;
import co.edu.atlas.auth.dto.LoginRequest;
import co.edu.atlas.auth.dto.TokenResponse;
import co.edu.atlas.auth.jwt.JwtProperties;
import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.token.RefreshToken;
import co.edu.atlas.auth.token.RefreshTokenRepository;
import co.edu.atlas.auth.token.Tokens;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    private static final JwtProperties JWT = new JwtProperties("x".repeat(32), Duration.ofMinutes(15), Duration.ofDays(7));
    private static final AuthProperties AUTH = new AuthProperties("http://front", Duration.ofMinutes(30), Duration.ofSeconds(60));

    @Mock UsuarioRepository usuarios;
    @Mock RefreshTokenRepository refreshTokens;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;

    MutableClock clock = new MutableClock();

    private AuthService servicio() {
        return new AuthService(usuarios, refreshTokens, encoder, jwt, JWT, AUTH, clock);
    }

    private static Usuario ana() {
        return new Usuario("ana@correo.com", "HASH", RolPerfil.ESTUDIANTE, RolSistema.USER);
    }

    private static RefreshToken refresh(String plano, java.time.Instant expira) {
        return new RefreshToken(7L, Tokens.hash(plano), expira);
    }

    @Test
    void loginEmiteTokensYGuardaRefreshSoloHasheado() {
        Usuario ana = ana();
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        when(encoder.matches("clave", "HASH")).thenReturn(true);
        when(jwt.generarAccessToken(ana)).thenReturn("ACCESS");
        when(jwt.accessTtlSegundos()).thenReturn(900L);

        TokenResponse r = servicio().login(new LoginRequest(" Ana@Correo.com ", "clave"));

        assertThat(r.accessToken()).isEqualTo("ACCESS");
        assertThat(r.tokenType()).isEqualTo("Bearer");
        assertThat(r.expiresIn()).isEqualTo(900L);
        ArgumentCaptor<RefreshToken> guardado = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokens).save(guardado.capture());
        assertThat(guardado.getValue().getTokenHash()).isEqualTo(Tokens.hash(r.refreshToken()));
        assertThat(guardado.getValue().getExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(7)));
    }

    @Test
    void loginConCorreoInexistenteHaceHashFalsoYFallaSinRevelarCual() {
        when(encoder.encode("contrasena-inexistente")).thenReturn("FALSO");
        when(usuarios.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().login(new LoginRequest("nadie@correo.com", "clave")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("CREDENCIALES_INVALIDAS"));
        verify(encoder).matches("clave", "FALSO");
        verifyNoInteractions(refreshTokens);
    }

    @Test
    void loginConPasswordIncorrectaFalla() {
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana()));
        when(encoder.matches("mala", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> servicio().login(new LoginRequest("ana@correo.com", "mala")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("CREDENCIALES_INVALIDAS"));
        verify(refreshTokens, never()).save(any());
    }

    @Test
    void loginDeUsuarioInactivoFalla() {
        Usuario ana = ana();
        ana.setActivo(false);
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> servicio().login(new LoginRequest("ana@correo.com", "clave")))
                .isInstanceOf(ApiException.class);
        verify(refreshTokens, never()).save(any());
    }

    @Test
    void refrescarRotaElTokenYEmiteUnoNuevo() {
        Usuario ana = ana();
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo")))
                .thenReturn(Optional.of(refresh("viejo", clock.instant().plusSeconds(3600))));
        when(usuarios.findById(7L)).thenReturn(Optional.of(ana));
        when(refreshTokens.revocarSiVigente(any(), eq(clock.instant()))).thenReturn(1);
        when(jwt.generarAccessToken(ana)).thenReturn("ACCESS2");

        TokenResponse r = servicio().refrescar("viejo");

        assertThat(r.accessToken()).isEqualTo("ACCESS2");
        assertThat(r.refreshToken()).isNotEqualTo("viejo");
    }

    @Test
    void refrescarConTokenRevocadoCierraTodasLasSesiones() {
        RefreshToken revocado = refresh("viejo", clock.instant().plusSeconds(3600));
        revocado.revocar(clock.instant().minusSeconds(10));
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo"))).thenReturn(Optional.of(revocado));

        assertThatThrownBy(() -> servicio().refrescar("viejo"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("REFRESH_INVALIDO"));
        verify(refreshTokens).revocarTodosDe(any(), eq(clock.instant()));
    }

    @Test
    void refrescarConCierrePendienteVencidoAvisaSesionExpirada() {
        RefreshToken t = refresh("viejo", clock.instant().plusSeconds(3600));
        t.programarCierre(clock.instant().minusSeconds(1));
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo"))).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> servicio().refrescar("viejo"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("SESION_EXPIRADA"));
    }

    @Test
    void refrescarConTokenExpiradoFalla() {
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo")))
                .thenReturn(Optional.of(refresh("viejo", clock.instant().minusSeconds(1))));

        assertThatThrownBy(() -> servicio().refrescar("viejo"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("REFRESH_INVALIDO"));
    }

    @Test
    void refrescarConTokenDesconocidoFalla() {
        when(refreshTokens.findByTokenHash(Tokens.hash("nada"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().refrescar("nada")).isInstanceOf(ApiException.class);
    }

    @Test
    void refrescarDeUsuarioInactivoFallaSinRotar() {
        Usuario ana = ana();
        ana.setActivo(false);
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo")))
                .thenReturn(Optional.of(refresh("viejo", clock.instant().plusSeconds(3600))));
        when(usuarios.findById(7L)).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> servicio().refrescar("viejo")).isInstanceOf(ApiException.class);
        verify(refreshTokens, never()).revocarSiVigente(any(), any());
    }

    @Test
    void refrescarConUsoConcurrenteTratarloComoReutilizacion() {
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo")))
                .thenReturn(Optional.of(refresh("viejo", clock.instant().plusSeconds(3600))));
        when(usuarios.findById(7L)).thenReturn(Optional.of(ana()));
        when(refreshTokens.revocarSiVigente(any(), eq(clock.instant()))).thenReturn(0);

        assertThatThrownBy(() -> servicio().refrescar("viejo")).isInstanceOf(ApiException.class);
        verify(refreshTokens).revocarTodosDe(any(), eq(clock.instant()));
    }

    @Test
    void logoutRevocaElTokenYToleraDesconocidos() {
        RefreshToken t = refresh("viejo", clock.instant().plusSeconds(3600));
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo"))).thenReturn(Optional.of(t));
        when(refreshTokens.findByTokenHash(Tokens.hash("nada"))).thenReturn(Optional.empty());

        servicio().logout("viejo");
        servicio().logout("nada");

        assertThat(t.estaRevocado()).isTrue();
        assertThat(t.getRevokedAt()).isEqualTo(clock.instant());
    }

    @Test
    void programarCierreDaLaGraciaDeUnMinuto() {
        RefreshToken t = refresh("viejo", clock.instant().plusSeconds(3600));
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo"))).thenReturn(Optional.of(t));

        servicio().programarCierre("viejo");

        assertThat(t.cierreVencido(clock.instant().plusSeconds(60))).isFalse();
        assertThat(t.cierreVencido(clock.instant().plusSeconds(61))).isTrue();
    }

    @Test
    void programarCierreDeTokenRevocadoNoHaceNada() {
        RefreshToken t = refresh("viejo", clock.instant().plusSeconds(3600));
        t.revocar(clock.instant());
        when(refreshTokens.findByTokenHash(Tokens.hash("viejo"))).thenReturn(Optional.of(t));

        servicio().programarCierre("viejo");

        assertThat(t.cierreVencido(clock.instant().plusSeconds(3600))).isFalse();
    }

    @Test
    void perfilDevuelveElUsuarioAutenticado() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(ana()));

        assertThat(servicio().perfil(1L).correo()).isEqualTo("ana@correo.com");
    }

    @Test
    void perfilDeUsuarioBorradoFallaComoNoAutenticado() {
        when(usuarios.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().perfil(1L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("NO_AUTENTICADO"));
    }
}
