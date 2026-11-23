package co.edu.atlas.metricas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/** Tope de visitas por usuario y minuto contra el inflado de metricas. */
@Import(TestClockConfig.class)
@TestPropertySource(properties = "app.metricas.max-visitas-por-minuto=3")
class LimiteVisitasTests extends ApiIntegrationTest {

    @Autowired
    VisitaRepository visitas;

    @Autowired
    MutableClock clock;

    private Map<String, Object> ficha(long id) {
        return Map.of("tipo", "FICHA", "ejercicioId", id);
    }

    @Test
    void userOverTheLimitGetsTooManyRequestsAndNothingIsStored() throws Exception {
        Usuario carla = usuario();
        long a = crearPublicada(admin(), "A");
        long antes = visitas.count();
        for (int i = 0; i < 3; i++) {
            mvc.perform(jsonDe(post("/metricas/visitas"), carla, ficha(a))).andExpect(status().isNoContent());
            clock.advance(Duration.ofSeconds(11)); // fuera de la ventana de duplicados
        }

        mvc.perform(jsonDe(post("/metricas/visitas"), carla, ficha(a)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("DEMASIADOS_INTENTOS"));
        assertThat(visitas.count()).isEqualTo(antes + 3);
    }

    @Test
    void limitIsPerUserAndResetsAfterTheWindow() throws Exception {
        Usuario carla = usuario();
        Usuario beto = usuario();
        long a = crearPublicada(admin(), "B");
        for (int i = 0; i < 3; i++) {
            mvc.perform(jsonDe(post("/metricas/visitas"), carla, ficha(a))).andExpect(status().isNoContent());
            clock.advance(Duration.ofSeconds(11));
        }
        mvc.perform(jsonDe(post("/metricas/visitas"), carla, ficha(a))).andExpect(status().isTooManyRequests());

        mvc.perform(jsonDe(post("/metricas/visitas"), beto, ficha(a))).andExpect(status().isNoContent());

        clock.advance(Duration.ofSeconds(61));
        mvc.perform(jsonDe(post("/metricas/visitas"), carla, ficha(a))).andExpect(status().isNoContent());
    }
}
