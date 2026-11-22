package co.edu.atlas.auth.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.common.error.ApiException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceTests {

    @Mock
    UsuarioRepository usuarios;

    @InjectMocks
    UsuarioAdminService service;

    @Test
    void promoteSetsAdminRole() {
        Usuario ana = new Usuario("ana@correo.com", "HASH", RolPerfil.ESTUDIANTE, RolSistema.USER);
        when(usuarios.findById(2L)).thenReturn(Optional.of(ana));

        assertThat(service.promover(2L).rolSistema()).isEqualTo(RolSistema.ADMIN);
        assertThat(ana.getRolSistema()).isEqualTo(RolSistema.ADMIN);
    }

    @Test
    void promoteUnknownUserFailsWithNotFound() {
        when(usuarios.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.promover(99L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("USUARIO_NO_ENCONTRADO"));
    }
}
