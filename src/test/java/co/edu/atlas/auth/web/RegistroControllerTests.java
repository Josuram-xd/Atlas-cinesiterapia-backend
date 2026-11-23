package co.edu.atlas.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistroControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder encoder;

    private ResultActions registrar(String json) throws Exception {
        return mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void registersNewAccountWithChosenRole() throws Exception {
        registrar("{\"correo\":\"nuevo@correo.com\",\"password\":\"secreta123\",\"rolPerfil\":\"PRACTICANTE\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("nuevo@correo.com"))
                .andExpect(jsonPath("$.rolPerfil").value("PRACTICANTE"))
                .andExpect(jsonPath("$.rolSistema").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        Usuario guardado = usuarios.findByCorreo("nuevo@correo.com").orElseThrow();
        assertThat(guardado.getPasswordHash()).isNotEqualTo("secreta123");
        assertThat(encoder.matches("secreta123", guardado.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        String json = "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\",\"rolPerfil\":\"ESTUDIANTE\"}";
        registrar(json).andExpect(status().isCreated());

        registrar(json.replace("ana@", "ANA@"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CORREO_EN_USO"));
    }

    @Test
    void rejectsMissingRole() throws Exception {
        registrar("{\"correo\":\"a@correo.com\",\"password\":\"secreta123\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("rolPerfil"));
    }

    @Test
    void rejectsUnknownRole() throws Exception {
        registrar("{\"correo\":\"a@correo.com\",\"password\":\"secreta123\",\"rolPerfil\":\"ADMIN\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidEmailAndShortPassword() throws Exception {
        registrar("{\"correo\":\"no-es-correo\",\"password\":\"corta\",\"rolPerfil\":\"VISITANTE\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }
}
