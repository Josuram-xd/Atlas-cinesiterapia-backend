package co.edu.atlas.metricas;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TiempoPromedioTests extends ApiIntegrationTest {

    static final String URL = "/admin/metricas/tiempo-promedio";

    @Autowired
    SesionUsoRepository sesiones;

    Usuario admin;
    Usuario carla;

    @BeforeEach
    void preparar() {
        admin = admin();
        carla = usuario();
    }

    private void sesion(String inicio, int segundos) {
        SesionUso s = new SesionUso(carla.getId(), Instant.parse(inicio));
        s.registrarPing(Instant.parse(inicio).plusSeconds(segundos));
        sesiones.save(s);
    }

    private String url(String desde, String hasta) {
        return URL + "?desde=" + desde + "&hasta=" + hasta;
    }

    @Test
    void averagesSessionDurationsInThePeriod() throws Exception {
        sesion("2026-03-02T10:00:00Z", 600);
        sesion("2026-03-03T10:00:00Z", 1200);
        sesion("2026-04-03T10:00:00Z", 9000); // fuera del periodo

        mvc.perform(getReq(url("2026-03-01", "2026-03-31"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesiones").value(2))
                .andExpect(jsonPath("$.promedioSegundos").value(900));
    }

    @Test
    void sessionsWithoutRecordedDurationAreIgnored() throws Exception {
        sesion("2026-03-02T10:00:00Z", 0);
        sesion("2026-03-03T10:00:00Z", 300);

        mvc.perform(getReq(url("2026-03-01", "2026-03-31"), admin))
                .andExpect(jsonPath("$.sesiones").value(1))
                .andExpect(jsonPath("$.promedioSegundos").value(300));
    }

    @Test
    void noDataMeansNullAverage() throws Exception {
        mvc.perform(getReq(url("2020-01-01", "2020-01-31"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesiones").value(0))
                .andExpect(jsonPath("$.promedioSegundos").doesNotExist())
                .andExpect(jsonPath("$.variacionPorcentaje").doesNotExist());
    }

    @Test
    void comparesWithThePreviousPeriodOfTheSameLength() throws Exception {
        sesion("2026-03-10T10:00:00Z", 1200); // periodo actual 10-12 mar
        sesion("2026-03-08T10:00:00Z", 600); // periodo anterior 7-9 mar

        mvc.perform(getReq(url("2026-03-10", "2026-03-12"), admin))
                .andExpect(jsonPath("$.promedioSegundos").value(1200))
                .andExpect(jsonPath("$.anterior.desde").value("2026-03-07"))
                .andExpect(jsonPath("$.anterior.hasta").value("2026-03-09"))
                .andExpect(jsonPath("$.anterior.sesiones").value(1))
                .andExpect(jsonPath("$.anterior.promedioSegundos").value(600))
                .andExpect(jsonPath("$.variacionPorcentaje").value(100.0));
    }

    @Test
    void noVariationWhenThePreviousPeriodHasNoData() throws Exception {
        sesion("2026-03-10T10:00:00Z", 1200);

        mvc.perform(getReq(url("2026-03-10", "2026-03-12"), admin))
                .andExpect(jsonPath("$.anterior.promedioSegundos").doesNotExist())
                .andExpect(jsonPath("$.variacionPorcentaje").doesNotExist());
    }

    @Test
    void defaultsToTheLastThirtyDaysAndValidatesTheRange() throws Exception {
        mvc.perform(getReq(URL, admin)).andExpect(status().isOk()).andExpect(jsonPath("$.desde").isString());
        mvc.perform(getReq(URL + "?hasta=2026-03-01", admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANGO_INVALIDO"));
        mvc.perform(getReq(url("2026-03-31", "2026-03-01"), admin)).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminsCanQuery() throws Exception {
        mvc.perform(getReq(URL, carla)).andExpect(status().isForbidden());
    }
}
