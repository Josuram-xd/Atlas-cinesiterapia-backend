package co.edu.atlas.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import co.edu.atlas.auth.token.RefreshTokenRepository;
import co.edu.atlas.auth.token.Tokens;
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
class AuthFlowTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    @Autowired
    RefreshTokenRepository refreshTokens;

    @BeforeEach
    void registrarAna() throws Exception {
        json("/auth/registro", "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\",\"rolPerfil\":\"ESTUDIANTE\"}")
                .andExpect(status().isCreated());
    }

    private ResultActions json(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String loginRefresh() throws Exception {
        String body = json("/auth/login", "{\"correo\":\"ana@correo.com\",\"password\":\"secreta123\"}")
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(body, "$.refreshToken");
    }

    private ResultActions refresh(String token) throws Exception {
        return json("/auth/refresh", "{\"refreshToken\":\"" + token + "\"}");
    }

    @Test
    void loginReturnsTokensAndUser() throws Exception {
        json("/auth/login", "{\"correo\":\" ANA@correo.com \",\"password\":\"secreta123\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.usuario.correo").value("ana@correo.com"));
    }

    @Test
    void refreshTokenIsStoredHashedOnly() throws Exception {
        String refresh = loginRefresh();

        assertThat(refreshTokens.findByTokenHash(Tokens.hash(refresh))).isPresent();
        assertThat(refreshTokens.findAll()).allSatisfy(t -> {
            assertThat(t.getTokenHash()).isNotEqualTo(refresh);
            assertThat(t.getTokenHash()).hasSize(64);
        });
    }

    @Test
    void wrongPasswordAndUnknownUserGetSameError() throws Exception {
        json("/auth/login", "{\"correo\":\"ana@correo.com\",\"password\":\"incorrecta1\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"));
        json("/auth/login", "{\"correo\":\"nadie@correo.com\",\"password\":\"secreta123\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void loginValidatesRequiredFields() throws Exception {
        json("/auth/login", "{\"correo\":\"\"}").andExpect(status().isBadRequest());
    }

    @Test
    void refreshRotatesTokenAndNewOneWorks() throws Exception {
        String primero = loginRefresh();

        String body = refresh(primero).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String segundo = JsonPath.read(body, "$.refreshToken");

        assertThat(segundo).isNotEqualTo(primero);
        refresh(segundo).andExpect(status().isOk());
    }

    @Test
    void reusingRotatedTokenRevokesEveryActiveSession() throws Exception {
        String primero = loginRefresh();
        String body = refresh(primero).andReturn().getResponse().getContentAsString();
        String segundo = JsonPath.read(body, "$.refreshToken");

        refresh(primero).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("REFRESH_INVALIDO"));

        refresh(segundo).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredRefreshTokenIsRejected() throws Exception {
        String token = loginRefresh();
        clock.advance(Duration.ofDays(8));

        refresh(token).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownRefreshTokenIsRejected() throws Exception {
        refresh("no-existe").andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshTokenImmediately() throws Exception {
        String token = loginRefresh();

        json("/auth/logout", "{\"refreshToken\":\"" + token + "\"}").andExpect(status().isNoContent());

        refresh(token).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutIsIdempotentForUnknownTokens() throws Exception {
        json("/auth/logout", "{\"refreshToken\":\"desconocido\"}").andExpect(status().isNoContent());
    }
}
