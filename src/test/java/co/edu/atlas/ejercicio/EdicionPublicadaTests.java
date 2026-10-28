package co.edu.atlas.ejercicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EdicionPublicadaTests extends ApiIntegrationTest {

    Usuario admin;
    long id;

    @BeforeEach
    void publicarFicha() throws Exception {
        admin = admin();
        id = crearBorrador(admin, fichaCompleta("Publicada"));
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("tipo", "IMAGEN");
        s.put("rol", "POSICION_INICIAL");
        s.put("contentType", "image/png");
        s.put("tamanoBytes", 1000);
        long media = cuerpo(mvc.perform(jsonDe(post("/admin/ejercicios/" + id + "/media/subida"), admin, s))
                        .andReturn())
                .get("mediaId")
                .asLong();
        mvc.perform(postReq("/admin/ejercicios/" + id + "/media/" + media + "/confirmar", admin))
                .andExpect(status().isOk());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin)).andExpect(status().isOk());
    }

    @Test
    void editingKeepsTheSheetPublishedWithNewValues() throws Exception {
        Map<String, Object> cambio = fichaCompleta("Publicada renombrada");
        cambio.put("indicaciones", "Indicación corregida");
        cambio.put("progresion", "Agregar resistencia elástica");

        mvc.perform(putJson("/admin/ejercicios/" + id, admin, cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.nombre").value("Publicada renombrada"))
                .andExpect(jsonPath("$.indicaciones").value("Indicación corregida"))
                .andExpect(jsonPath("$.publicadoEn").isNotEmpty())
                .andExpect(jsonPath("$.media.length()").value(1))
                .andExpect(jsonPath("$.camposFaltantes").isEmpty());
    }

    @Test
    void optionalFieldsCanBeClearedOnAPublishedSheet() throws Exception {
        Map<String, Object> con = fichaCompleta("Publicada");
        con.put("progresion", "algo");
        mvc.perform(putJson("/admin/ejercicios/" + id, admin, con)).andExpect(status().isOk());

        mvc.perform(putJson("/admin/ejercicios/" + id, admin, fichaCompleta("Publicada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progresion").doesNotExist());
    }

    @Test
    void emptyingARequiredFieldIsRejectedAndNothingIsSaved() throws Exception {
        Map<String, Object> cambio = fichaCompleta("Nombre que no debe guardarse");
        cambio.put("objetivoTerapeutico", "   ");
        cambio.remove("profesoraRevisora");

        mvc.perform(putJson("/admin/ejercicios/" + id, admin, cambio))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("FICHA_INCOMPLETA"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='objetivoTerapeutico')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='profesoraRevisora')]").exists());

        mvc.perform(getReq("/admin/ejercicios/" + id, admin))
                .andExpect(jsonPath("$.nombre").value("Publicada"))
                .andExpect(jsonPath("$.objetivoTerapeutico").value("Recuperar el rango de flexión"))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));
    }

    @Test
    void minimalBodyCannotOverwriteAPublishedSheet() throws Exception {
        mvc.perform(putJson("/admin/ejercicios/" + id, admin, fichaMinima("Vaciada")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.length()").value(17));
    }

    @Test
    void theLastMediaOfAPublishedSheetCannotBeRemoved() throws Exception {
        long media = cuerpo(mvc.perform(getReq("/admin/ejercicios/" + id, admin)).andReturn())
                .get("media").get(0).get("id").asLong();

        mvc.perform(deleteReq("/admin/ejercicios/" + id + "/media/" + media, admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEDIA_REQUERIDA"));
    }

    @Test
    void regionAndCategoryCanBeChangedOnAPublishedSheet() throws Exception {
        Map<String, Object> cambio = fichaCompleta("Publicada");
        cambio.put("regionId", regionId("posterior-lumbar"));
        cambio.put("categoriaId", categoriaId(5));

        mvc.perform(putJson("/admin/ejercicios/" + id, admin, cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region.svgId").value("posterior-lumbar"))
                .andExpect(jsonPath("$.categoria.nombre").value("Estiramiento y elongación"));
    }
}
