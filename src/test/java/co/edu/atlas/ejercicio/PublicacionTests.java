package co.edu.atlas.ejercicio;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PublicacionTests extends ApiIntegrationTest {

    /** Sube y confirma una imagen para la ficha. */
    private void agregarImagen(Usuario admin, long ficha) throws Exception {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("tipo", "IMAGEN");
        s.put("rol", "POSICION_INICIAL");
        s.put("contentType", "image/png");
        s.put("tamanoBytes", 1000);
        long media = cuerpo(mvc.perform(jsonDe(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                        "/admin/ejercicios/" + ficha + "/media/subida"),
                                admin, s))
                        .andReturn())
                .get("mediaId")
                .asLong();
        mvc.perform(postReq("/admin/ejercicios/" + ficha + "/media/" + media + "/confirmar", admin))
                .andExpect(status().isOk());
    }

    @Test
    void publishesACompleteSheet() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin, fichaCompleta("Flexión completa"));
        agregarImagen(admin, id);

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.publicadoEn").isNotEmpty())
                .andExpect(jsonPath("$.camposFaltantes").isEmpty());
    }

    @Test
    void publishesWithOptionalFieldsEmpty() throws Exception {
        Usuario admin = admin();
        Map<String, Object> ficha = fichaCompleta("Sin opcionales");
        ficha.remove("progresion");
        ficha.remove("erroresComunes");
        long id = crearBorrador(admin, ficha);
        agregarImagen(admin, id);

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin)).andExpect(status().isOk());
    }

    @Test
    void incompleteSheetIsRejectedListingEveryMissingField() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin, fichaMinima("Incompleta"));

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("FICHA_INCOMPLETA"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(18))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='objetivoTerapeutico')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='dosificacion.series')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='media')].message")
                        .value("Agrega al menos una imagen o un video"));

        mvc.perform(getReq("/admin/ejercicios/" + id, admin))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.camposFaltantes.length()").value(18));
    }

    @Test
    void completeSheetWithoutMediaIsRejected() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin, fichaCompleta("Sin medios"));

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("media"));
    }

    @Test
    void unconfirmedUploadDoesNotCountAsMedia() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin, fichaCompleta("Pendiente"));
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("tipo", "VIDEO");
        s.put("contentType", "video/mp4");
        s.put("tamanoBytes", 1000);
        mvc.perform(jsonDe(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/admin/ejercicios/" + id + "/media/subida"),
                        admin, s))
                .andExpect(status().isCreated());

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cannotPublishTwice() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin, fichaCompleta("Doble"));
        agregarImagen(admin, id);
        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin)).andExpect(status().isOk());

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ESTADO_INVALIDO"));
    }

    @Test
    void missingSheetIs404AndNonAdminForbidden() throws Exception {
        mvc.perform(postReq("/admin/ejercicios/99999/publicar", admin())).andExpect(status().isNotFound());
        mvc.perform(postReq("/admin/ejercicios/1/publicar", usuario())).andExpect(status().isForbidden());
    }
}
