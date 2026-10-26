package co.edu.atlas.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUsuarioControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    JwtService jwt;

    private Usuario crear(String correo, RolSistema rol) {
        return usuarios.save(new Usuario(correo, "hash", RolPerfil.ESTUDIANTE, rol));
    }

    private String bearer(Usuario u) {
        return "Bearer " + jwt.generarAccessToken(u);
    }

    @Test
    void adminPromotesAnotherUser() throws Exception {
        Usuario admin = crear("jefe@correo.com", RolSistema.ADMIN);
        Usuario destino = crear("nuevo@correo.com", RolSistema.USER);

        mvc.perform(post("/admin/usuarios/" + destino.getId() + "/promover").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolSistema").value("ADMIN"));

        assertThat(usuarios.findById(destino.getId()).orElseThrow().getRolSistema()).isEqualTo(RolSistema.ADMIN);
    }

    @Test
    void promotingAnAdminIsIdempotent() throws Exception {
        Usuario admin = crear("jefe@correo.com", RolSistema.ADMIN);

        mvc.perform(post("/admin/usuarios/" + admin.getId() + "/promover").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolSistema").value("ADMIN"));
    }

    @Test
    void regularUserCannotPromote() throws Exception {
        Usuario normal = crear("normal@correo.com", RolSistema.USER);

        mvc.perform(post("/admin/usuarios/" + normal.getId() + "/promover").header("Authorization", bearer(normal)))
                .andExpect(status().isForbidden());

        assertThat(usuarios.findById(normal.getId()).orElseThrow().getRolSistema()).isEqualTo(RolSistema.USER);
    }

    @Test
    void anonymousCannotPromote() throws Exception {
        mvc.perform(post("/admin/usuarios/1/promover")).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownUserIsNotFound() throws Exception {
        Usuario admin = crear("jefe@correo.com", RolSistema.ADMIN);

        mvc.perform(post("/admin/usuarios/999999/promover").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USUARIO_NO_ENCONTRADO"));
    }
}
