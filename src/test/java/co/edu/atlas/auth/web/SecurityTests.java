package co.edu.atlas.auth.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestClockConfig.class, SecurityTests.RolesController.class})
@Transactional
class SecurityTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtService jwt;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    MutableClock clock;

    private String bearer(RolSistema rol) {
        Usuario u = usuarios.save(new Usuario(rol + "@correo.com", "hash", RolPerfil.ESTUDIANTE, rol));
        return "Bearer " + jwt.generarAccessToken(u);
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorizedJson() throws Exception {
        mvc.perform(get("/ejercicios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));
    }

    @Test
    void garbageTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/auth/me").header("Authorization", "Bearer basura"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsUnauthorized() throws Exception {
        String token = bearer(RolSistema.USER);
        clock.advance(Duration.ofMinutes(20));

        mvc.perform(get("/auth/me").header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenReachesProtectedEndpoint() throws Exception {
        mvc.perform(get("/auth/me").header("Authorization", bearer(RolSistema.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("user@correo.com"))
                .andExpect(jsonPath("$.rolSistema").value("USER"));
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void otherActuatorEndpointsAreProtected() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnlyEndpointRejectsRegularUser() throws Exception {
        mvc.perform(get("/test-roles/admin").header("Authorization", bearer(RolSistema.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void adminOnlyEndpointAllowsAdmin() throws Exception {
        mvc.perform(get("/test-roles/admin").header("Authorization", bearer(RolSistema.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void adminPathsNeedAdminEvenWithoutPreAuthorize() throws Exception {
        mvc.perform(get("/admin/test-sin-anotacion").header("Authorization", bearer(RolSistema.USER)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/test-sin-anotacion").header("Authorization", bearer(RolSistema.ADMIN)))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @RestController
    static class RolesController {
        @GetMapping("/admin/test-sin-anotacion")
        String adminSinAnotacion() {
            return "ok";
        }

        @GetMapping("/test-roles/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String admin() {
            return "ok";
        }
    }
}
