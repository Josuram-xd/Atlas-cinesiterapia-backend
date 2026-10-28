package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchivadoEliminacionTests extends ApiIntegrationTest {

    @Test
    void archivingKeepsContentAndMovesToArchivedList() throws Exception {
        Usuario admin = admin();
        long id = crearPublicada(admin, "Para archivar");

        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ARCHIVADO"))
                .andExpect(jsonPath("$.archivadoEn").isNotEmpty())
                .andExpect(jsonPath("$.objetivoTerapeutico").value("Recuperar el rango de flexión"))
                .andExpect(jsonPath("$.media.length()").value(1));

        mvc.perform(getReq("/admin/ejercicios?estado=ARCHIVADO", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(getReq("/admin/ejercicios?estado=PUBLICADO", admin))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void onlyPublishedSheetsCanBeArchived() throws Exception {
        Usuario admin = admin();
        long borrador = crearBorrador(admin, fichaMinima("Borrador"));
        mvc.perform(postReq("/admin/ejercicios/" + borrador + "/archivar", admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ESTADO_INVALIDO"));

        long archivada = crearPublicada(admin, "Ya archivada");
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andExpect(status().isOk());
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andExpect(status().isConflict());
    }

    @Test
    void restoringReturnsToPublished() throws Exception {
        Usuario admin = admin();
        long id = crearPublicada(admin, "Para restaurar");
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", admin)).andExpect(status().isOk());

        mvc.perform(postReq("/admin/ejercicios/" + id + "/restaurar", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.archivadoEn").doesNotExist());
    }

    @Test
    void onlyArchivedSheetsCanBeRestored() throws Exception {
        Usuario admin = admin();
        long publicada = crearPublicada(admin, "Publicada");
        mvc.perform(postReq("/admin/ejercicios/" + publicada + "/restaurar", admin))
                .andExpect(status().isConflict());
        long borrador = crearBorrador(admin, fichaMinima("Borrador"));
        mvc.perform(postReq("/admin/ejercicios/" + borrador + "/restaurar", admin)).andExpect(status().isConflict());
    }

    @Test
    void archivedSheetStillRequiresCompleteContentWhenEdited() throws Exception {
        Usuario admin = admin();
        long id = crearPublicada(admin, "Archivada editable");
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", admin)).andExpect(status().isOk());

        Map<String, Object> incompleta = fichaCompleta("Archivada editable");
        incompleta.remove("ejecucion");
        mvc.perform(putJson("/admin/ejercicios/" + id, admin, incompleta))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(putJson("/admin/ejercicios/" + id, admin, fichaCompleta("Archivada y corregida")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ARCHIVADO"))
                .andExpect(jsonPath("$.nombre").value("Archivada y corregida"));
    }

    @Test
    void deleteIsSoftAndHidesTheSheetEverywhere() throws Exception {
        Usuario admin = admin();
        long id = crearPublicada(admin, "Para eliminar");

        mvc.perform(deleteReq("/admin/ejercicios/" + id, admin)).andExpect(status().isNoContent());

        mvc.perform(getReq("/admin/ejercicios/" + id, admin)).andExpect(status().isNotFound());
        mvc.perform(getReq("/admin/ejercicios", admin)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(putJson("/admin/ejercicios/" + id, admin, fichaCompleta("x"))).andExpect(status().isNotFound());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", admin)).andExpect(status().isNotFound());
        mvc.perform(deleteReq("/admin/ejercicios/" + id, admin)).andExpect(status().isNotFound());
    }

    @Test
    void anyStateCanBeDeleted() throws Exception {
        Usuario admin = admin();
        long borrador = crearBorrador(admin, fichaMinima("Borrador"));
        long archivada = crearPublicada(admin, "Archivada");
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andExpect(status().isOk());

        mvc.perform(deleteReq("/admin/ejercicios/" + borrador, admin)).andExpect(status().isNoContent());
        mvc.perform(deleteReq("/admin/ejercicios/" + archivada, admin)).andExpect(status().isNoContent());

        assertThat(cuerpo(mvc.perform(getReq("/admin/ejercicios", admin)).andReturn()).get("totalElements").asInt())
                .isZero();
    }

    @Test
    void nonAdminsCannotChangeState() throws Exception {
        Usuario user = usuario();
        mvc.perform(postReq("/admin/ejercicios/1/archivar", user)).andExpect(status().isForbidden());
        mvc.perform(postReq("/admin/ejercicios/1/restaurar", user)).andExpect(status().isForbidden());
        mvc.perform(deleteReq("/admin/ejercicios/1", user)).andExpect(status().isForbidden());
    }
}
