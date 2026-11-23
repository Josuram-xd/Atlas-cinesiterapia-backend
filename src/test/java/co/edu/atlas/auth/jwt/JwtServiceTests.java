package co.edu.atlas.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.atlas.auth.security.AuthenticatedUser;
import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTests {

    private static final String SECRET = "unit-test-secret-unit-test-secret-0123";

    private final MutableClock clock = new MutableClock();
    private final JwtService jwt =
            new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(15), Duration.ofDays(7)), clock);

    private Usuario usuario(RolSistema rol) {
        Usuario u = new Usuario("ana@correo.com", "hash", RolPerfil.ESTUDIANTE, rol);
        ReflectionTestUtils.setField(u, "id", 42L);
        return u;
    }

    @Test
    void roundTripsClaims() {
        AuthenticatedUser parsed = jwt.parsear(jwt.generarAccessToken(usuario(RolSistema.ADMIN)));

        assertThat(parsed).isEqualTo(new AuthenticatedUser(42L, "ana@correo.com", RolSistema.ADMIN));
    }

    @Test
    void rejectsExpiredToken() {
        String token = jwt.generarAccessToken(usuario(RolSistema.USER));
        clock.advance(Duration.ofMinutes(16));

        assertThatThrownBy(() -> jwt.parsear(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTamperedAndForeignTokens() {
        String token = jwt.generarAccessToken(usuario(RolSistema.USER));
        JwtService otro = new JwtService(
                new JwtProperties("another-secret-another-secret-012345", Duration.ofMinutes(15), Duration.ofDays(7)),
                clock);

        assertThatThrownBy(() -> jwt.parsear(token + "x")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> otro.parsear(token)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwt.parsear("basura")).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsShortSecret() {
        JwtProperties corto = new JwtProperties("corto", Duration.ofMinutes(1), Duration.ofDays(1));

        assertThatThrownBy(() -> new JwtService(corto, clock)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exposesAccessTtlInSeconds() {
        assertThat(jwt.accessTtlSegundos()).isEqualTo(900);
    }
}
