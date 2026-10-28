package co.edu.atlas.auth.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

/** BCrypt solo admite 72 bytes: una clave mas larga debe ser un 400 y no un 500. Igual para correos y tokens enormes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordLengthTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private void enviar(String url, Map<String, Object> cuerpo, int esperado) throws Exception {
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(cuerpo)))
                .andExpect(status().is(esperado));
    }

    @Test
    void loginWithOverlongPasswordIsBadRequest() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("correo", "a@correo.com", "password", "x".repeat(200)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void overlongEmailsAndTokensAreBadRequest() throws Exception {
        String largo = "x".repeat(300);
        enviar("/auth/login", Map.of("correo", largo, "password", "secreta123"), 400);
        enviar("/auth/recuperar", Map.of("correo", largo + "@correo.com"), 400);
        enviar("/auth/refresh", Map.of("refreshToken", largo), 400);
        enviar("/auth/restablecer", Map.of("token", largo, "nuevaPassword", "secreta123"), 400);
    }

    @Test
    void multibytePasswordOver72BytesIsRejectedEverywhere() throws Exception {
        String clave = "ñ".repeat(40); // 40 caracteres, 80 bytes
        enviar("/auth/login", Map.of("correo", "a@correo.com", "password", clave), 400);
        enviar("/auth/registro", Map.of("correo", "b@correo.com", "password", clave, "rolPerfil", "ESTUDIANTE"), 400);
        enviar("/auth/restablecer", Map.of("token", "abc", "nuevaPassword", clave), 400);
    }
}
