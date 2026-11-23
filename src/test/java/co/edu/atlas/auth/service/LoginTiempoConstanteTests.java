package co.edu.atlas.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.dto.LoginRequest;
import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.token.RefreshTokenRepository;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/** El login siempre compara un hash, exista o no la cuenta y este o no activa, para no filtrarlo por tiempo. */
class LoginTiempoConstanteTests {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthService auth = new AuthService(
            usuarios, mock(RefreshTokenRepository.class), encoder, mock(JwtService.class), null, null, new MutableClock());

    @Test
    void cuentaInactivaCompruebaLaClaveYFalla() {
        Usuario ana = new Usuario("ana@correo.com", "HASH", RolPerfil.ESTUDIANTE, RolSistema.USER);
        ana.setActivo(false);
        when(usuarios.findByCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        when(encoder.matches("clave", "HASH")).thenReturn(true);

        assertThatThrownBy(() -> auth.login(new LoginRequest("ana@correo.com", "clave")))
                .isInstanceOf(ApiException.class);
        verify(encoder).matches("clave", "HASH");
    }

    @Test
    void correoInexistenteTambienCompruebaUnHash() {
        when(usuarios.findByCorreo(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auth.login(new LoginRequest("nadie@correo.com", "clave")))
                .isInstanceOf(ApiException.class);
        verify(encoder).matches(eq("clave"), any());
    }
}
