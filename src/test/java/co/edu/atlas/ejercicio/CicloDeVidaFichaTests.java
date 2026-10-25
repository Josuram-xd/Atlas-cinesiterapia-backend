package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Recorrido completo de una ficha, de borrador a eliminada, como lo haria el front de administracion. */
class CicloDeVidaFichaTests extends ApiIntegrationTest {

    @Test
    void sheetGoesFromDraftToDeletedStepByStep() throws Exception {
        Usuario ana = admin();
        Usuario luis = admin();

        // 1. Borrador con solo la informacion general (HU-04, HU-10)
        long id = crearBorrador(ana, fichaMinima("Flexión pasiva de rodilla"));
        JsonNode borrador = cuerpo(mvc.perform(getReq("/admin/ejercicios/" + id, ana)).andReturn());
        assertThat(borrador.get("estado").asText()).isEqualTo("BORRADOR");
        assertThat(borrador.get("camposFaltantes").size()).isEqualTo(18);

        // 2. Intento de publicar incompleta (HU-11)
        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", ana))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.length()").value(18));

        // 3. Se completa el contenido por partes (HU-05 a HU-07, HU-09)
        mvc.perform(putJson("/admin/ejercicios/" + id, ana, fichaCompleta("Flexión pasiva de rodilla")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.camposFaltantes.length()").value(1))
                .andExpect(jsonPath("$.camposFaltantes[0]").value("media"));

        // 4. Video e imagenes por URL prefirmada (HU-08)
        Map<String, Object> video = new LinkedHashMap<>();
        video.put("tipo", "VIDEO");
        video.put("contentType", "video/mp4");
        video.put("tamanoBytes", 2_000_000);
        long videoId = cuerpo(mvc.perform(jsonDe(post("/admin/ejercicios/" + id + "/media/subida"), ana, video))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("mediaId")
                .asLong();
        mvc.perform(postReq("/admin/ejercicios/" + id + "/media/" + videoId + "/confirmar", ana))
                .andExpect(status().isOk());
        agregarImagen(ana, id);

        // 5. Publicacion (HU-11) por otra administradora (HU-15)
        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.media.length()").value(2))
                .andExpect(jsonPath("$.media[0].tipo").value("VIDEO"))
                .andExpect(jsonPath("$.camposFaltantes").isEmpty());

        // 6. Edicion de la ficha publicada (HU-12)
        Map<String, Object> cambio = fichaCompleta("Flexión pasiva de rodilla");
        cambio.put("progresion", "Pasar a movilización activo-asistida");
        mvc.perform(putJson("/admin/ejercicios/" + id, ana, cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.progresion").value("Pasar a movilización activo-asistida"));

        // 7. Archivado y restauracion (HU-14)
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", ana))
                .andExpect(jsonPath("$.estado").value("ARCHIVADO"));
        mvc.perform(getReq("/admin/ejercicios?estado=ARCHIVADO", luis))
                .andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(postReq("/admin/ejercicios/" + id + "/restaurar", luis))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));

        // 8. Eliminacion (HU-13)
        mvc.perform(deleteReq("/admin/ejercicios/" + id, ana)).andExpect(status().isNoContent());
        mvc.perform(getReq("/admin/ejercicios/" + id, luis)).andExpect(status().isNotFound());
        mvc.perform(getReq("/admin/ejercicios", luis)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void invalidTransitionsNeverCorruptTheSheet() throws Exception {
        Usuario admin = admin();
        long id = crearPublicada(admin, "Transiciones");

        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin)).andExpect(status().isConflict());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/restaurar", admin)).andExpect(status().isConflict());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", admin)).andExpect(status().isOk());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", admin)).andExpect(status().isConflict());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", admin)).andExpect(status().isConflict());

        mvc.perform(getReq("/admin/ejercicios/" + id, admin))
                .andExpect(jsonPath("$.estado").value("ARCHIVADO"))
                .andExpect(jsonPath("$.media.length()").value(1))
                .andExpect(jsonPath("$.nombre").value("Transiciones"));
    }

    @Test
    void anonymousAndRegularUsersCannotTouchAdminEndpoints() throws Exception {
        Usuario admin = admin();
        long id = crearPublicada(admin, "Protegida");
        Usuario user = usuario();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/ejercicios/" + id))
                .andExpect(status().isUnauthorized());
        mvc.perform(getReq("/admin/ejercicios/" + id, user)).andExpect(status().isForbidden());
        mvc.perform(putJson("/admin/ejercicios/" + id, user, fichaCompleta("Hackeada"))).andExpect(status().isForbidden());
        mvc.perform(deleteReq("/admin/ejercicios/" + id, user)).andExpect(status().isForbidden());

        mvc.perform(getReq("/admin/ejercicios/" + id, admin)).andExpect(jsonPath("$.nombre").value("Protegida"));
    }
}
