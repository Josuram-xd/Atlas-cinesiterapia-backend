package co.edu.atlas.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** La revocacion es una actualizacion condicional: solo el primero que la pide gana (evita bifurcar sesiones). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenClaimTests {

    @Autowired
    RefreshTokenRepository refresh;

    @Autowired
    PasswordResetTokenRepository resets;

    @Autowired
    UsuarioRepository usuarios;

    private Long usuarioId() {
        return usuarios.save(new Usuario("claim@correo.com", "hash", RolPerfil.ESTUDIANTE, RolSistema.USER)).getId();
    }

    @Test
    void refreshTokenCanBeClaimedOnlyOnce() {
        RefreshToken t = refresh.save(new RefreshToken(usuarioId(), "hash-claim-1", Instant.now().plusSeconds(60)));

        assertThat(refresh.revocarSiVigente(t.getId(), Instant.now())).isEqualTo(1);
        assertThat(refresh.revocarSiVigente(t.getId(), Instant.now())).isZero();
    }

    @Test
    void resetTokenCanBeClaimedOnlyOnce() {
        PasswordResetToken t =
                resets.save(new PasswordResetToken(usuarioId(), "hash-claim-2", Instant.now().plusSeconds(60)));

        assertThat(resets.marcarUsadoSiVigente(t.getId(), Instant.now())).isEqualTo(1);
        assertThat(resets.marcarUsadoSiVigente(t.getId(), Instant.now())).isZero();
    }
}
