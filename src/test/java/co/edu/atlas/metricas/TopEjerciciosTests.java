package co.edu.atlas.metricas;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TopEjerciciosTests extends ApiIntegrationTest {

    static final String URL = "/admin/metricas/ejercicios-mas-consultados";

    @Autowired
    VisitaRepository visitas;

    Usuario admin;
    Usuario carla;

    @BeforeEach
    void preparar() {
        admin = admin();
        carla = usuario();
    }

    private void vistas(long ejercicio, int veces, String instante) {
        for (int i = 0; i < veces; i++) {
            visitas.save(new Visita(carla.getId(), TipoVisita.FICHA, ejercicio, Instant.parse(instante)));
        }
    }

    @Test
    void ranksSheetsFromMostToLeastViewedWithRegionAndCategory() throws Exception {
        long a = publicada(admin, "Alfa", "anterior-rodilla", 1);
        long b = publicada(admin, "Bravo", "anterior-codo", 2);
        long c = publicada(admin, "Charlie", "anterior-rodilla", 1);
        vistas(a, 2, "2026-03-01T10:00:00Z");
        vistas(b, 5, "2026-03-01T10:00:00Z");
        vistas(c, 3, "2026-03-02T10:00:00Z");

        mvc.perform(getReq(URL, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].ejercicioId").value(b))
                .andExpect(jsonPath("$.items[0].nombre").value("Bravo"))
                .andExpect(jsonPath("$.items[0].visitas").value(5))
                .andExpect(jsonPath("$.items[0].region.svgId").value("anterior-codo"))
                .andExpect(jsonPath("$.items[0].categoria.nombre").value("Movilización activo-asistida"))
                .andExpect(jsonPath("$.items[1].ejercicioId").value(c))
                .andExpect(jsonPath("$.items[2].ejercicioId").value(a));
    }

    @Test
    void respectsTheLimitAndDefaultsToTen() throws Exception {
        for (int i = 0; i < 12; i++) {
            long id = publicada(admin, "Ficha " + i, "anterior-rodilla", 1);
            vistas(id, 1 + i, "2026-03-01T10:00:00Z");
        }

        mvc.perform(getReq(URL, admin)).andExpect(jsonPath("$.items.length()").value(10)).andExpect(jsonPath("$.limite").value(10));
        mvc.perform(getReq(URL + "?limite=3", admin))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].visitas").value(12));
    }

    @Test
    void onlyListsSheetsThatHaveViews() throws Exception {
        long a = publicada(admin, "Con visitas", "anterior-rodilla", 1);
        publicada(admin, "Sin visitas", "anterior-codo", 1);
        vistas(a, 1, "2026-03-01T10:00:00Z");

        mvc.perform(getReq(URL, admin))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].ejercicioId").value(a));
    }

    @Test
    void emptyRankingWhenThereAreNoViews() throws Exception {
        mvc.perform(getReq(URL, admin)).andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void atlasVisitsAreNotCounted() throws Exception {
        visitas.save(new Visita(carla.getId(), TipoVisita.ATLAS, null, Instant.parse("2026-03-01T10:00:00Z")));

        mvc.perform(getReq(URL, admin)).andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void filtersByDateRange() throws Exception {
        long a = publicada(admin, "Alfa", "anterior-rodilla", 1);
        long b = publicada(admin, "Bravo", "anterior-codo", 1);
        vistas(a, 4, "2026-02-01T10:00:00Z");
        vistas(b, 1, "2026-03-10T10:00:00Z");

        mvc.perform(getReq(URL + "?desde=2026-03-01&hasta=2026-03-31", admin))
                .andExpect(jsonPath("$.desde").value("2026-03-01"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].ejercicioId").value(b));
    }

    @Test
    void archivedAndDeletedSheetsLeaveTheRanking() throws Exception {
        long a = publicada(admin, "Archivada", "anterior-rodilla", 1);
        long b = publicada(admin, "Eliminada", "anterior-codo", 1);
        long c = publicada(admin, "Vigente", "anterior-codo", 2);
        vistas(a, 3, "2026-03-01T10:00:00Z");
        vistas(b, 3, "2026-03-01T10:00:00Z");
        vistas(c, 1, "2026-03-01T10:00:00Z");
        mvc.perform(postReq("/admin/ejercicios/" + a + "/archivar", admin)).andExpect(status().isOk());
        mvc.perform(deleteReq("/admin/ejercicios/" + b, admin)).andExpect(status().isNoContent());

        mvc.perform(getReq(URL, admin))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].ejercicioId").value(c));
    }

    @Test
    void validatesParameters() throws Exception {
        mvc.perform(getReq(URL + "?limite=0", admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq(URL + "?limite=51", admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq(URL + "?desde=2026-03-01", admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANGO_INVALIDO"));
        mvc.perform(getReq(URL + "?desde=2026-03-10&hasta=2026-03-01", admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANGO_INVALIDO"));
    }

    @Test
    void onlyAdminsCanQuery() throws Exception {
        mvc.perform(getReq(URL, carla)).andExpect(status().isForbidden());
    }
}
