package co.edu.atlas.common.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = {"app.rate-limit.max-requests=3", "app.rate-limit.window=1m"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
class RateLimitFilterTests {

    private static final String LOGIN = "{\"correo\":\"nadie@correo.com\",\"password\":\"secreta123\"}";

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    private ResultActions enviar(String url, String ip) throws Exception {
        return mvc.perform(post(url)
                .with(r -> {
                    r.setRemoteAddr(ip);
                    return r;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN));
    }

    @Test
    void loginIsBlockedAfterMaxAttemptsWithRetryAfter() throws Exception {
        String ip = "10.0.0.1";
        for (int i = 0; i < 3; i++) {
            enviar("/auth/login", ip).andExpect(status().isUnauthorized());
        }

        enviar("/auth/login", ip)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.code").value("DEMASIADOS_INTENTOS"));
    }

    @Test
    void registroHasItsOwnCounterAndOtherClientsAreNotAffected() throws Exception {
        String ip = "10.0.0.2";
        for (int i = 0; i < 3; i++) {
            enviar("/auth/registro", ip).andExpect(status().isBadRequest());
        }

        enviar("/auth/registro", ip).andExpect(status().isTooManyRequests());
        enviar("/auth/login", ip).andExpect(status().isUnauthorized());
        enviar("/auth/registro", "10.0.0.99").andExpect(status().isBadRequest());
    }

    @Test
    void allowsAgainWhenWindowPasses() throws Exception {
        String ip = "10.0.0.3";
        for (int i = 0; i < 4; i++) {
            enviar("/auth/login", ip);
        }
        enviar("/auth/login", ip).andExpect(status().isTooManyRequests());

        clock.advance(Duration.ofSeconds(61));

        enviar("/auth/login", ip).andExpect(status().isUnauthorized());
    }

    @Test
    void otherRoutesAreNotLimited() throws Exception {
        for (int i = 0; i < 6; i++) {
            mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        }
    }
}
