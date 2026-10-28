package co.edu.atlas.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.AuthProperties;
import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.token.PasswordResetToken;
import co.edu.atlas.auth.token.PasswordResetTokenRepository;
import co.edu.atlas.auth.token.RefreshTokenRepository;
import co.edu.atlas.auth.token.Tokens;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.mail.CorreoSender;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTests {

    private static final AuthProperties AUTH = new AuthProperties("http://front", Duration.ofMinutes(30), Duration.ofSeconds(60));

    @Mock UsuarioRepository usuarios;
    @Mock PasswordResetTokenRepository resetTokens;
    @Mock RefreshTokenRepository refreshTokens;
    @Mock PasswordEncoder encoder;
    @Mock CorreoSender correo;

    MutableClock clock = new MutableClock();

    private PasswordResetService servicio() {
        return new PasswordResetService(usuarios, resetTokens, refreshTokens, encoder, correo, AUTH, clock);
    }

    private static Usuario ana() {
        return new Usuario("ana@correo.com", "HASH", RolPerfil.ESTUDIANTE, RolSistema.USER);
    }

    @Test
    void solicitarEmiteTokenHasheadoYEnviaEnlaceConElMismoToken() {
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana()));

        servicio().solicitar(" Ana@Correo.com ");

        verify(resetTokens).invalidarPendientes(any(), eq(clock.instant()));
        verify(resetTokens).save(any(PasswordResetToken.class));
        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(correo, timeout(2000)).enviar(eq("ana@correo.com"), anyString(), cuerpo.capture());
        Matcher m = Pattern.compile("token=(\\S+)").matcher(cuerpo.getValue());
        assertThat(m.find()).isTrue();
        assertThat(cuerpo.getValue()).contains("http://front/restablecer-password?token=");
    }

    @Test
    void solicitarConCorreoInexistenteNoHaceNada() {
        when(usuarios.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        servicio().solicitar("nadie@correo.com");

        verifyNoInteractions(resetTokens, correo);
    }

    @Test
    void solicitarDeUsuarioInactivoNoHaceNada() {
        Usuario ana = ana();
        ana.setActivo(false);
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana));

        servicio().solicitar("ana@correo.com");

        verifyNoInteractions(resetTokens, correo);
    }

    @Test
    void falloDelCorreoNoSePropagaNiBorraElToken() {
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana()));
        doThrow(new RuntimeException("smtp caido")).when(correo).enviar(anyString(), anyString(), anyString());

        assertThatCode(() -> servicio().solicitar("ana@correo.com")).doesNotThrowAnyException();
        verify(resetTokens).save(any());
        verify(correo, timeout(2000)).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void restablecerCambiaLaClaveYRevocaLasSesiones() {
        Usuario ana = ana();
        PasswordResetToken t = new PasswordResetToken(3L, Tokens.hash("tok"), clock.instant().plusSeconds(1800));
        when(resetTokens.findByTokenHash(Tokens.hash("tok"))).thenReturn(Optional.of(t));
        when(resetTokens.marcarUsadoSiVigente(any(), eq(clock.instant()))).thenReturn(1);
        when(usuarios.findById(3L)).thenReturn(Optional.of(ana));
        when(encoder.encode("nueva123")).thenReturn("NUEVO_HASH");

        servicio().restablecer("tok", "nueva123");

        assertThat(ana.getPasswordHash()).isEqualTo("NUEVO_HASH");
        verify(refreshTokens).revocarTodosDe(any(), eq(clock.instant()));
    }

    @Test
    void restablecerConTokenDesconocidoOVencidoFallaComoInvalido() {
        PasswordResetToken vencido = new PasswordResetToken(3L, Tokens.hash("tok"), clock.instant().minusSeconds(1));
        when(resetTokens.findByTokenHash(Tokens.hash("tok"))).thenReturn(Optional.of(vencido));

        assertThatThrownBy(() -> servicio().restablecer("tok", "nueva123"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("TOKEN_INVALIDO"));
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void restablecerQuePierdeLaCarreraNoCambiaLaClave() {
        PasswordResetToken t = new PasswordResetToken(3L, Tokens.hash("tok"), clock.instant().plusSeconds(1800));
        when(resetTokens.findByTokenHash(Tokens.hash("tok"))).thenReturn(Optional.of(t));
        when(resetTokens.marcarUsadoSiVigente(any(), eq(clock.instant()))).thenReturn(0);

        assertThatThrownBy(() -> servicio().restablecer("tok", "nueva123"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("TOKEN_INVALIDO"));
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void restablecerDeUsuarioBorradoFallaComoInvalido() {
        PasswordResetToken t = new PasswordResetToken(3L, Tokens.hash("tok"), clock.instant().plusSeconds(1800));
        when(resetTokens.findByTokenHash(Tokens.hash("tok"))).thenReturn(Optional.of(t));
        when(resetTokens.marcarUsadoSiVigente(any(), eq(clock.instant()))).thenReturn(1);
        when(usuarios.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().restablecer("tok", "nueva123"))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("TOKEN_INVALIDO"));
        verify(refreshTokens, never()).revocarTodosDe(any(), any());
    }
}
