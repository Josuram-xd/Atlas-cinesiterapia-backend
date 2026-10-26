package co.edu.atlas.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.dto.RegistroRequest;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistroServiceTests {

    @Mock
    UsuarioRepository usuarios;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    RegistroService service;

    @Test
    void createsUserWithNormalizedEmailHashedPasswordAndUserRole() {
        when(usuarios.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(encoder.encode("secreta123")).thenReturn("HASH");
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = service.registrar(new RegistroRequest(" Ana@Correo.com ", "secreta123", RolPerfil.ESTUDIANTE));

        assertThat(u.getCorreo()).isEqualTo("ana@correo.com");
        assertThat(u.getPasswordHash()).isEqualTo("HASH");
        assertThat(u.getRolPerfil()).isEqualTo(RolPerfil.ESTUDIANTE);
        assertThat(u.getRolSistema()).isEqualTo(RolSistema.USER);
    }

    @Test
    void rejectsExistingEmail() {
        when(usuarios.existsByCorreo("ana@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(new RegistroRequest("ana@correo.com", "secreta123", RolPerfil.VISITANTE)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("CORREO_EN_USO"));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void mapsRaceConditionOnUniqueConstraintToConflict() {
        when(usuarios.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(encoder.encode("secreta123")).thenReturn("HASH");
        when(usuarios.saveAndFlush(any(Usuario.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.registrar(new RegistroRequest("ana@correo.com", "secreta123", RolPerfil.VISITANTE)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("CORREO_EN_USO"));
    }
}
