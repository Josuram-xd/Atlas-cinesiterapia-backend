package co.edu.atlas.auth.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminSeedServiceTests {

    @Mock
    UsuarioRepository usuarios;

    @Mock
    PasswordEncoder encoder;

    private AdminSeedService servicio(String email, String password) {
        return new AdminSeedService(new AdminProperties(email, password), usuarios, encoder);
    }

    @Test
    void createsAdminWhenConfiguredAndMissing() {
        when(usuarios.existsByCorreo("root@atlas.test")).thenReturn(false);
        when(encoder.encode("claveSegura1")).thenReturn("HASH");

        servicio(" Root@Atlas.test ", "claveSegura1").sembrar();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertThat(captor.getValue().getCorreo()).isEqualTo("root@atlas.test");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASH");
        assertThat(captor.getValue().getRolSistema()).isEqualTo(RolSistema.ADMIN);
    }

    @Test
    void doesNothingWhenAlreadyExists() {
        when(usuarios.existsByCorreo("root@atlas.test")).thenReturn(true);

        servicio("root@atlas.test", "claveSegura1").sembrar();

        verify(usuarios, never()).save(any());
    }

    @Test
    void doesNothingWhenNotConfigured() {
        servicio("", "").sembrar();
        servicio("root@atlas.test", "").sembrar();

        verify(usuarios, never()).save(any());
    }

    @Test
    void doesNothingWhenPasswordTooShort() {
        servicio("root@atlas.test", "corta").sembrar();

        verify(usuarios, never()).save(any());
    }
}
