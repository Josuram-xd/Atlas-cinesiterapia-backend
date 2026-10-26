package co.edu.atlas.auth.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.admin.email=semilla@atlas.test", "app.admin.password=claveSemilla1"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSeedStartupTests {

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    AdminSeedService seed;

    @Autowired
    MockMvc mvc;

    @Test
    void firstAdminIsCreatedAtStartupAndCanLogin() throws Exception {
        assertThat(usuarios.findByCorreo("semilla@atlas.test"))
                .hasValueSatisfying(u -> assertThat(u.getRolSistema()).isEqualTo(RolSistema.ADMIN));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"semilla@atlas.test\",\"password\":\"claveSemilla1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rolSistema").value("ADMIN"));
    }

    @Test
    void seedingTwiceDoesNotDuplicate() {
        seed.sembrar();
        seed.sembrar();

        assertThat(usuarios.findAll().stream().filter(u -> u.getCorreo().equals("semilla@atlas.test"))).hasSize(1);
    }
}
