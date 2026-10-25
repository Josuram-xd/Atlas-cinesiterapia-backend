package co.edu.atlas.auth.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTests {

    @Autowired
    UsuarioRepository repo;

    @Test
    void savesAndFindsByNormalizedEmail() {
        Usuario guardado = repo.saveAndFlush(
                new Usuario("  Ana@Correo.COM ", "hash", RolPerfil.ESTUDIANTE, RolSistema.USER));

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getCreatedAt()).isNotNull();
        assertThat(repo.findByCorreo("ana@correo.com")).isPresent();
        assertThat(repo.existsByCorreo("ana@correo.com")).isTrue();
        assertThat(repo.existsByCorreo("otra@correo.com")).isFalse();
    }

    @Test
    void defaultsToActiveUser() {
        Usuario u = repo.saveAndFlush(new Usuario("a@b.co", "hash", RolPerfil.VISITANTE, RolSistema.USER));

        assertThat(u.isActivo()).isTrue();
        assertThat(u.getRolSistema()).isEqualTo(RolSistema.USER);
    }

    @Test
    void rejectsDuplicateEmail() {
        repo.saveAndFlush(new Usuario("dup@correo.com", "hash", RolPerfil.PROFESIONAL, RolSistema.USER));

        assertThatThrownBy(() ->
                        repo.saveAndFlush(new Usuario("DUP@correo.com", "h2", RolPerfil.PRACTICANTE, RolSistema.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
