package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListadoAtlasTests extends ApiIntegrationTest {

    Usuario admin;
    Usuario lector;

    @BeforeEach
    void preparar() {
        admin = admin();
        lector = usuario();
    }

    // ---- 3.2 listado por region y categoria ----

    @Test
    void listsOnlyPublishedSheetsOfRegionAndCategorySortedByName() throws Exception {
        long b = publicada(admin, "Bravo", "anterior-rodilla", 1);
        long a = publicada(admin, "Alfa", "anterior-rodilla", 1);
        publicada(admin, "Otra categoria", "anterior-rodilla", 2);
        publicada(admin, "Otra region", "anterior-codo", 1);
        crearBorrador(admin, fichaMinima("Borrador"));

        mvc.perform(getReq("/ejercicios?region=" + regionId("anterior-rodilla") + "&categoria=" + categoriaId(1), lector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(a))
                .andExpect(jsonPath("$[0].nombre").value("Alfa"))
                .andExpect(jsonPath("$[1].id").value(b))
                .andExpect(jsonPath("$[0].region.svgId").value("anterior-rodilla"))
                .andExpect(jsonPath("$[0].categoria.nombre").value("Movilización pasiva"))
                .andExpect(jsonPath("$[0].estado").doesNotExist())
                .andExpect(jsonPath("$[0].creadoPor").doesNotExist());
    }

    @Test
    void archivedAndDeletedSheetsDisappearFromTheList() throws Exception {
        long archivada = publicada(admin, "Archivada", "anterior-rodilla", 1);
        long eliminada = publicada(admin, "Eliminada", "anterior-rodilla", 1);
        publicada(admin, "Visible", "anterior-rodilla", 1);
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andExpect(status().isOk());
        mvc.perform(deleteReq("/admin/ejercicios/" + eliminada, admin)).andExpect(status().isNoContent());

        mvc.perform(getReq("/ejercicios", lector))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Visible"));
    }

    @Test
    void filtersAreOptionalAndEmptyResultIsAnEmptyArray() throws Exception {
        publicada(admin, "Uno", "anterior-rodilla", 1);
        publicada(admin, "Dos", "anterior-codo", 3);

        mvc.perform(getReq("/ejercicios", lector)).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(getReq("/ejercicios?region=" + regionId("anterior-codo"), lector))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(getReq("/ejercicios?categoria=" + categoriaId(9), lector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void nonNumericFilterIsBadRequestAndAnonymousIsUnauthorized() throws Exception {
        mvc.perform(getReq("/ejercicios?region=abc", lector)).andExpect(status().isBadRequest());
        mvc.perform(get("/ejercicios")).andExpect(status().isUnauthorized());
    }
}
