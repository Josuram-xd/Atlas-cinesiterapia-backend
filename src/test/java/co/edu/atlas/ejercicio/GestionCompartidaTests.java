package co.edu.atlas.ejercicio;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;

/** HU-15: cualquier administrador ve y gestiona las fichas creadas por otro. */
class GestionCompartidaTests extends ApiIntegrationTest {

    @Test
    void anotherAdminListsAndReadsSheetsCreatedByTheFirst() throws Exception {
        Usuario ana = admin();
        Usuario luis = admin();
        long id = crearBorrador(ana, fichaCompleta("Ficha de Ana"));

        mvc.perform(getReq("/admin/ejercicios", luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].creadoPor.id").value(ana.getId()))
                .andExpect(jsonPath("$.content[0].creadoPor.correo").value(ana.getCorreo()));
        mvc.perform(getReq("/admin/ejercicios/" + id, luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivoTerapeutico").value("Recuperar el rango de flexión"))
                .andExpect(jsonPath("$.creadoPor.id").value(ana.getId()));
    }

    @Test
    void anotherAdminEditsWithoutChangingTheOriginalCreator() throws Exception {
        Usuario ana = admin();
        Usuario luis = admin();
        long id = crearBorrador(ana, fichaCompleta("Ficha de Ana"));

        mvc.perform(putJson("/admin/ejercicios/" + id, luis, fichaCompleta("Editada por Luis")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editada por Luis"))
                .andExpect(jsonPath("$.creadoPor.id").value(ana.getId()))
                .andExpect(jsonPath("$.actualizadoPor.id").value(luis.getId()))
                .andExpect(jsonPath("$.actualizadoPor.correo").value(luis.getCorreo()));
    }

    @Test
    void anotherAdminRunsTheWholeLifecycle() throws Exception {
        Usuario ana = admin();
        Usuario luis = admin();
        long id = crearBorrador(ana, fichaCompleta("Ciclo compartido"));

        agregarImagen(luis, id);
        mvc.perform(postReq("/admin/ejercicios/" + id + "/publicar", luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.actualizadoPor.id").value(luis.getId()));
        mvc.perform(postReq("/admin/ejercicios/" + id + "/archivar", luis)).andExpect(status().isOk());
        mvc.perform(postReq("/admin/ejercicios/" + id + "/restaurar", luis)).andExpect(status().isOk());
        mvc.perform(deleteReq("/admin/ejercicios/" + id, luis)).andExpect(status().isNoContent());
        mvc.perform(getReq("/admin/ejercicios/" + id, ana)).andExpect(status().isNotFound());
    }

    @Test
    void aPromotedUserGainsAccessOnlyAfterPromotion() throws Exception {
        Usuario ana = admin();
        long id = crearBorrador(ana, fichaMinima("Ficha"));
        Usuario nuevo = usuario();

        mvc.perform(getReq("/admin/ejercicios/" + id, nuevo)).andExpect(status().isForbidden());

        mvc.perform(postReq("/admin/usuarios/" + nuevo.getId() + "/promover", ana)).andExpect(status().isOk());
        usuarios.flush();
        Usuario promovido = usuarios.findById(nuevo.getId()).orElseThrow();
        mvc.perform(getReq("/admin/ejercicios/" + id, promovido)).andExpect(status().isOk());
    }
}
