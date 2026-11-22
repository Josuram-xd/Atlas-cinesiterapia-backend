package co.edu.atlas.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import co.edu.atlas.common.mail.CorreoSender;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@Transactional
class PasswordResetTests {

    private static final Pattern TOKEN = Pattern.compile("token=([A-Za-z0-9_-]+)");

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    @MockitoBean
    CorreoSender correo;

    @BeforeEach
    void registrarAna() throws Exception {
        json("/auth/registro", "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\",\"rolPerfil\":\"ESTUDIANTE\"}")
                .andExpect(status().isCreated());
    }

    private ResultActions json(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions recuperar(String correoSolicitado) throws Exception {
        return json("/auth/recuperar", "{\"correo\":\"" + correoSolicitado + "\"}");
    }

    private ResultActions restablecer(String token, String password) throws Exception {
        return json("/auth/restablecer", "{\"token\":\"" + token + "\",\"nuevaPassword\":\"" + password + "\"}");
    }

    private String ultimoTokenEnviado() {
        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(correo, timeout(2000).times(1)).enviar(eq("ana@correo.com"), anyString(), cuerpo.capture());
        Matcher m = TOKEN.matcher(cuerpo.getValue());
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    private ResultActions login(String password) throws Exception {
        return json("/auth/login", "{\"correo\":\"ana@correo.com\",\"password\":\"" + password + "\"}");
    }

    @Test
    void registeredEmailGetsLinkAndGenericResponse() throws Exception {
        recuperar("ana@correo.com")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        assertThat(ultimoTokenEnviado()).hasSizeGreaterThan(20);
    }

    @Test
    void unknownEmailGetsSameResponseAndNoMail() throws Exception {
        String conocido = recuperar("ana@correo.com").andReturn().getResponse().getContentAsString();
        String desconocido = recuperar("noexiste@correo.com")
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(JsonPath.<String>read(desconocido, "$.mensaje")).isEqualTo(JsonPath.<String>read(conocido, "$.mensaje"));
        verify(correo, never()).enviar(eq("noexiste@correo.com"), anyString(), anyString());
    }

    @Test
    void newPasswordWorksAndOldOneStops() throws Exception {
        recuperar("ana@correo.com");
        String token = ultimoTokenEnviado();

        restablecer(token, "nuevaClave456").andExpect(status().isNoContent());

        login("nuevaClave456").andExpect(status().isOk());
        login("secreta123").andExpect(status().isUnauthorized());
    }

    @Test
    void tokenIsSingleUse() throws Exception {
        recuperar("ana@correo.com");
        String token = ultimoTokenEnviado();
        restablecer(token, "nuevaClave456").andExpect(status().isNoContent());

        restablecer(token, "otraClave789")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALIDO"));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        recuperar("ana@correo.com");
        String token = ultimoTokenEnviado();
        clock.advance(Duration.ofMinutes(31));

        restablecer(token, "nuevaClave456").andExpect(status().isBadRequest());
        login("secreta123").andExpect(status().isOk());
    }

    @Test
    void unknownTokenAndWeakPasswordAreRejected() throws Exception {
        restablecer("inventado", "nuevaClave456").andExpect(status().isBadRequest());
        restablecer("inventado", "corta").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void resetRevokesExistingSessions() throws Exception {
        String body = login("secreta123").andReturn().getResponse().getContentAsString();
        String refresh = JsonPath.read(body, "$.refreshToken");
        recuperar("ana@correo.com");
        String token = ultimoTokenEnviado();

        restablecer(token, "nuevaClave456").andExpect(status().isNoContent());

        json("/auth/refresh", "{\"refreshToken\":\"" + refresh + "\"}").andExpect(status().isUnauthorized());
    }

    @Test
    void mailFailureDoesNotRevealOrBreakTheRequest() throws Exception {
        doThrow(new IllegalStateException("smtp caido")).when(correo).enviar(anyString(), anyString(), anyString());

        recuperar("ana@correo.com").andExpect(status().isAccepted());
    }
}
