package co.edu.atlas.metricas;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VisitasPeriodoTests extends ApiIntegrationTest {

    @Autowired
    VisitaRepository visitas;

    Usuario admin;
    Usuario carla;

    @BeforeEach
    void preparar() {
        admin = admin();
        carla = usuario();
    }

    private void visita(String instante) {
        visitas.save(new Visita(carla.getId(), TipoVisita.ATLAS, null, Instant.parse(instante)));
    }

    private String url(String desde, String hasta, String agrupacion) {
        return "/admin/metricas/visitas?desde=" + desde + "&hasta=" + hasta + (agrupacion == null ? "" : "&agrupacion=" + agrupacion);
    }

    @Test
    void dailySeriesFillsEmptyDaysWithZero() throws Exception {
        visita("2026-03-02T10:00:00Z");
        visita("2026-03-02T23:59:59Z");
        visita("2026-03-04T00:00:00Z");
        visita("2026-03-05T00:00:00Z"); // fuera del rango

        mvc.perform(getReq(url("2026-03-01", "2026-03-04", null), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agrupacion").value("DIA"))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.serie.length()").value(4))
                .andExpect(jsonPath("$.serie[0].inicio").value("2026-03-01"))
                .andExpect(jsonPath("$.serie[0].visitas").value(0))
                .andExpect(jsonPath("$.serie[1].visitas").value(2))
                .andExpect(jsonPath("$.serie[2].visitas").value(0))
                .andExpect(jsonPath("$.serie[3].visitas").value(1));
    }

    @Test
    void weeklySeriesStartsOnMondays() throws Exception {
        visita("2026-03-01T10:00:00Z"); // domingo, semana del lunes 23-feb
        visita("2026-03-02T10:00:00Z"); // lunes
        visita("2026-03-08T10:00:00Z"); // domingo, misma semana
        visita("2026-03-09T10:00:00Z"); // lunes siguiente

        mvc.perform(getReq(url("2026-02-23", "2026-03-15", "SEMANA"), admin))
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.serie.length()").value(3))
                .andExpect(jsonPath("$.serie[0].inicio").value("2026-02-23"))
                .andExpect(jsonPath("$.serie[0].visitas").value(1))
                .andExpect(jsonPath("$.serie[1].inicio").value("2026-03-02"))
                .andExpect(jsonPath("$.serie[1].visitas").value(2))
                .andExpect(jsonPath("$.serie[2].inicio").value("2026-03-09"))
                .andExpect(jsonPath("$.serie[2].visitas").value(1));
    }

    @Test
    void weeklyBucketKeepsOnlyVisitsInsideTheRange() throws Exception {
        visita("2026-03-02T10:00:00Z");
        visita("2026-03-04T10:00:00Z");

        mvc.perform(getReq(url("2026-03-04", "2026-03-04", "SEMANA"), admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.serie.length()").value(1))
                .andExpect(jsonPath("$.serie[0].inicio").value("2026-03-02"))
                .andExpect(jsonPath("$.serie[0].visitas").value(1));
    }

    @Test
    void monthlySeriesGroupsByCalendarMonth() throws Exception {
        visita("2026-01-31T23:00:00Z");
        visita("2026-02-01T00:00:00Z");
        visita("2026-02-28T12:00:00Z");
        visita("2026-04-10T12:00:00Z");

        mvc.perform(getReq(url("2026-01-15", "2026-04-30", "MES"), admin))
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.serie.length()").value(4))
                .andExpect(jsonPath("$.serie[0].inicio").value("2026-01-01"))
                .andExpect(jsonPath("$.serie[0].visitas").value(1))
                .andExpect(jsonPath("$.serie[1].inicio").value("2026-02-01"))
                .andExpect(jsonPath("$.serie[1].visitas").value(2))
                .andExpect(jsonPath("$.serie[2].visitas").value(0))
                .andExpect(jsonPath("$.serie[3].visitas").value(1));
    }

    @Test
    void periodWithoutVisitsReturnsZero() throws Exception {
        mvc.perform(getReq(url("2020-01-01", "2020-01-03", null), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.serie.length()").value(3))
                .andExpect(jsonPath("$.serie[0].visitas").value(0));
    }

    @Test
    void acceptsTheMaximumRangeOfOneYearAndRejectsLonger() throws Exception {
        mvc.perform(getReq(url("2026-01-01", "2026-12-31", "MES"), admin)).andExpect(status().isOk());
        mvc.perform(getReq(url("2024-01-01", "2024-12-31", "DIA"), admin)).andExpect(status().isOk()); // 366 dias
        mvc.perform(getReq(url("2025-01-01", "2026-01-02", null), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANGO_INVALIDO"));
    }

    @Test
    void rejectsInvertedRangeAndMalformedParameters() throws Exception {
        mvc.perform(getReq(url("2026-03-10", "2026-03-01", null), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANGO_INVALIDO"));
        mvc.perform(getReq(url("ayer", "2026-03-01", null), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        mvc.perform(getReq(url("2026-03-01", "2026-03-02", "ANIO"), admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq("/admin/metricas/visitas?desde=2026-03-01", admin)).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminsCanQuery() throws Exception {
        mvc.perform(getReq(url("2026-03-01", "2026-03-02", null), carla)).andExpect(status().isForbidden());
    }
}
