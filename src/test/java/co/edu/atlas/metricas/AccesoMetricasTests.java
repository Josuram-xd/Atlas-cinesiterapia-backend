package co.edu.atlas.metricas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class AccesoMetricasTests extends ApiIntegrationTest {

    /** Todas las rutas de consulta de metricas: solo ADMIN. Se amplia con cada metrica. */
    static final String[] RUTAS_ADMIN = {
        "/admin/metricas",
        "/admin/metricas/visitas?desde=2026-01-01&hasta=2026-01-31",
        "/admin/metricas/ejercicios-mas-consultados",
        "/admin/metricas/usuarios-activos",
        "/admin/metricas/tiempo-promedio",
    };

    @Autowired
    VisitaRepository visitas;

    @Test
    void adminSeesTheSummary() throws Exception {
        Usuario admin = admin();
        visitas.save(new Visita(admin.getId(), TipoVisita.ATLAS, null, java.time.Instant.now()));

        mvc.perform(getReq("/admin/metricas", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitasTotales").value(visitas.count()))
                .andExpect(jsonPath("$.visitasUltimos30Dias").isNumber())
                .andExpect(jsonPath("$.sesionesTotales").isNumber());
    }

    @Test
    void nonAdminUsersAreDeniedEveryMetricsRoute() throws Exception {
        Usuario normal = usuario();
        for (String ruta : RUTAS_ADMIN) {
            mvc.perform(getReq(ruta, normal)).andExpect(status().isForbidden());
        }
    }

    @Test
    void anonymousRequestsAreRejectedOnEveryMetricsRoute() throws Exception {
        for (String ruta : RUTAS_ADMIN) {
            mvc.perform(get(ruta)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void registeringActivityDoesNotGrantAccessToAggregates() throws Exception {
        Usuario normal = usuario();
        mvc.perform(jsonDe(MockMvcRequestBuilders.post("/metricas/visitas"), normal, Map.of("tipo", "ATLAS")))
                .andExpect(status().isNoContent());

        mvc.perform(getReq("/admin/metricas", normal)).andExpect(status().isForbidden());
    }
}
