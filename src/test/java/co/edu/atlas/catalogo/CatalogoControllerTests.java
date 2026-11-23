package co.edu.atlas.catalogo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;

class CatalogoControllerTests extends ApiIntegrationTest {

    @Test
    void listsAllRegionsOrderedByViewThenOrder() throws Exception {
        mvc.perform(getReq("/regiones", usuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(27))
                .andExpect(jsonPath("$[0].svgId").value("anterior-cabeza-cuello"))
                .andExpect(jsonPath("$[0].vista").value("ANTERIOR"))
                .andExpect(jsonPath("$[0].orden").value(1))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[13].svgId").value("posterior-cabeza-cuello"))
                .andExpect(jsonPath("$[26].svgId").value("posterior-tobillo-pie"));
    }

    @Test
    void filtersRegionsByView() throws Exception {
        mvc.perform(getReq("/regiones?vista=POSTERIOR", usuario()))
                .andExpect(jsonPath("$.length()").value(14))
                .andExpect(jsonPath("$[?(@.vista=='ANTERIOR')]").isEmpty());
        mvc.perform(getReq("/regiones?vista=ANTERIOR", usuario())).andExpect(jsonPath("$.length()").value(13));
    }

    @Test
    void unknownViewIsBadRequest() throws Exception {
        mvc.perform(getReq("/regiones?vista=LATERAL", usuario())).andExpect(status().isBadRequest());
    }

    @Test
    void listsTheNineCategories() throws Exception {
        mvc.perform(getReq("/categorias", usuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].nombre").value("Movilización pasiva"))
                .andExpect(jsonPath("$[8].nombre").value("Facilitación neuromuscular propioceptiva (FNP)"));
    }

    @Test
    void regionCategoriesCountOnlyPublishedSheets() throws Exception {
        Usuario admin = admin();
        crearPublicada(admin, "Publicada en rodilla");
        crearBorrador(admin, fichaMinima("Borrador en rodilla"));
        long archivada = crearPublicada(admin, "Archivada en rodilla");
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andReturn();
        long eliminada = crearPublicada(admin, "Eliminada en rodilla");
        mvc.perform(deleteReq("/admin/ejercicios/" + eliminada, admin)).andReturn();

        mvc.perform(getReq("/regiones/" + regionId("anterior-rodilla") + "/categorias", usuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].nombre").value("Movilización pasiva"))
                .andExpect(jsonPath("$[0].totalEjercicios").value(1))
                .andExpect(jsonPath("$[1].totalEjercicios").value(0));
    }

    @Test
    void countsAreScopedToTheRegion() throws Exception {
        crearPublicada(admin(), "En rodilla");

        mvc.perform(getReq("/regiones/" + regionId("posterior-lumbar") + "/categorias", usuario()))
                .andExpect(jsonPath("$[0].totalEjercicios").value(0));
    }

    @Test
    void unknownRegionIs404() throws Exception {
        mvc.perform(getReq("/regiones/99999/categorias", usuario()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REGION_NO_ENCONTRADA"));
    }

    @Test
    void catalogRequiresLogin() throws Exception {
        mvc.perform(get("/regiones")).andExpect(status().isUnauthorized());
        mvc.perform(get("/categorias")).andExpect(status().isUnauthorized());
        mvc.perform(get("/regiones/1/categorias")).andExpect(status().isUnauthorized());
    }
}
