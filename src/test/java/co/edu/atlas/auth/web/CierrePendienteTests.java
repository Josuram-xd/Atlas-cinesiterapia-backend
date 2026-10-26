package co.edu.atlas.auth.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class CierrePendienteTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    private String refreshToken;

    @BeforeEach
    void iniciarSesion() throws Exception {
        json("/auth/registro", "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\",\"rolPerfil\":\"ESTUDIANTE\"}");
        String body = json("/auth/login", "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\"}")
                .andReturn()
                .getResponse()
                .getContentAsString();
        refreshToken = JsonPath.read(body, "$.refreshToken");
    }

    private ResultActions json(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions token(String url, String token) throws Exception {
        return json(url, "{\"refreshToken\":\"" + token + "\"}");
    }

    @Test
    void refreshWithinGraceKeepsSessionAndCancelsPendingClose() throws Exception {
        token("/auth/cierre-pendiente", refreshToken).andExpect(status().isNoContent());
        clock.advance(Duration.ofSeconds(59));

        String body = token("/auth/refresh", refreshToken)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String nuevo = JsonPath.read(body, "$.refreshToken");

        clock.advance(Duration.ofMinutes(10));
        token("/auth/refresh", nuevo).andExpect(status().isOk());
    }

    @Test
    void refreshAfterGraceIsRejectedAsExpiredSession() throws Exception {
        token("/auth/cierre-pendiente", refreshToken).andExpect(status().isNoContent());
        clock.advance(Duration.ofSeconds(61));

        token("/auth/refresh", refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESION_EXPIRADA"));
        token("/auth/refresh", refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESION_EXPIRADA"));
    }

    @Test
    void expiredGraceDoesNotAffectOtherSessionsOfTheSameUser() throws Exception {
        String body = json("/auth/login", "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\"}")
                .andReturn()
                .getResponse()
                .getContentAsString();
        String otraSesion = JsonPath.read(body, "$.refreshToken");
        token("/auth/cierre-pendiente", refreshToken);
        clock.advance(Duration.ofSeconds(90));

        token("/auth/refresh", refreshToken).andExpect(status().isUnauthorized());
        token("/auth/refresh", refreshToken).andExpect(status().isUnauthorized());

        token("/auth/refresh", otraSesion).andExpect(status().isOk());
    }

    @Test
    void sessionWithoutPendingCloseSurvivesLongIdle() throws Exception {
        clock.advance(Duration.ofHours(2));

        token("/auth/refresh", refreshToken).andExpect(status().isOk());
    }

    @Test
    void secondPendingCallDoesNotExtendTheGracePeriod() throws Exception {
        token("/auth/cierre-pendiente", refreshToken);
        clock.advance(Duration.ofSeconds(40));
        token("/auth/cierre-pendiente", refreshToken);
        clock.advance(Duration.ofSeconds(30));

        token("/auth/refresh", refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    void explicitLogoutInvalidatesImmediatelyWithoutWaitingForGrace() throws Exception {
        token("/auth/logout", refreshToken).andExpect(status().isNoContent());

        token("/auth/refresh", refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownOrBlankTokenDoesNotLeakInformation() throws Exception {
        token("/auth/cierre-pendiente", "desconocido").andExpect(status().isNoContent());
        token("/auth/cierre-pendiente", "").andExpect(status().isBadRequest());
    }
}
