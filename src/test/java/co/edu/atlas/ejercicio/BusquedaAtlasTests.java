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

class BusquedaAtlasTests extends ApiIntegrationTest {

    Usuario admin;
    Usuario lector;

    @BeforeEach
    void preparar() {
        admin = admin();
        lector = usuario();
    }

    // ---- 3.4 busqueda ----

    @Test
    void searchIgnoresCaseAndAccentsAndMatchesPartially() throws Exception {
        publicada(admin, "Flexión de rodilla", "anterior-rodilla", 1);
        publicada(admin, "Extensión de rodilla", "anterior-rodilla", 1);
        publicada(admin, "Puente de glúteo", "posterior-gluteo", 1);

        mvc.perform(getReq("/ejercicios/buscar?q=FLEXION", lector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Flexión de rodilla"));
        mvc.perform(getReq("/ejercicios/buscar?q=rodil", lector))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Extensión de rodilla"));
        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "glúteo"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "  de   rodilla "))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchOnlyReturnsPublishedSheets() throws Exception {
        crearBorrador(admin, fichaMinima("Sentadilla borrador"));
        long archivada = publicada(admin, "Sentadilla archivada", "anterior-muslo", 1);
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andExpect(status().isOk());
        publicada(admin, "Sentadilla publicada", "anterior-muslo", 1);

        mvc.perform(getReq("/ejercicios/buscar?q=sentadilla", lector))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Sentadilla publicada"));
    }

    @Test
    void searchWithoutMatchesIsAnEmptyArray() throws Exception {
        publicada(admin, "Algo", "anterior-rodilla", 1);

        mvc.perform(getReq("/ejercicios/buscar?q=inexistente", lector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void blankOrTooLongQueryIsRejected() throws Exception {
        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", ""))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "x".repeat(101)))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(getReq("/ejercicios/buscar", lector)).andExpect(status().isBadRequest());
    }

    @Test
    void searchTreatsWildcardsLiterallyAndRequiresLogin() throws Exception {
        publicada(admin, "Avance al 100%", "anterior-rodilla", 1);
        publicada(admin, "Otro", "anterior-rodilla", 1);

        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "%"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "_"))
                .andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/ejercicios/buscar").param("q", "a")).andExpect(status().isUnauthorized());
    }

    @Test
    void searchCapsResultsAtFifty() throws Exception {
        Usuario a = admin;
        for (int i = 0; i < 52; i++) {
            Map<String, Object> ficha = fichaCompleta("Masivo " + String.format("%02d", i));
            long id = crearBorrador(a, ficha);
            agregarImagen(a, id);
            mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", a)).andExpect(status().isOk());
        }

        var r = cuerpo(mvc.perform(getReq("/ejercicios/buscar?q=masivo", lector)).andReturn());
        assertThat(r.size()).isEqualTo(50);
        assertThat(r.get(0).get("nombre").asText()).isEqualTo("Masivo 00");
    }
}
