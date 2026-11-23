package co.edu.atlas.comentario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ColaRevisionTests extends ApiIntegrationTest {

    @Autowired
    ComentarioRepository comentarios;

    Usuario admin;
    Usuario carla;
    Usuario luis;
    Usuario ana;
    long ficha;

    @BeforeEach
    void preparar() throws Exception {
        admin = admin();
        carla = usuario();
        luis = usuario();
        ana = usuario();
        ficha = crearPublicada(admin, "Flexión de rodilla");
    }

    long comentar(Usuario u, String texto) throws Exception {
        return cuerpo(mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), u, Map.of("texto", texto)))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    void reportar(Usuario u, long comentario) throws Exception {
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", u)).andExpect(status().isCreated());
    }

    @Test
    void emptyQueueWhenNothingIsPending() throws Exception {
        comentar(carla, "Todo bien");

        mvc.perform(getReq("/admin/comentarios/revision", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listsAutoHiddenComments() throws Exception {
        long oculto = comentar(carla, "eres un idiota");

        mvc.perform(getReq("/admin/comentarios/revision", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oculto))
                .andExpect(jsonPath("$.content[0].texto").value("eres un idiota"))
                .andExpect(jsonPath("$.content[0].ocultadoAutomaticamente").value(true))
                .andExpect(jsonPath("$.content[0].totalReportes").value(0))
                .andExpect(jsonPath("$.content[0].autor.correo").value(carla.getCorreo()))
                .andExpect(jsonPath("$.content[0].ejercicio.id").value(ficha))
                .andExpect(jsonPath("$.content[0].ejercicio.nombre").value("Flexión de rodilla"));
    }

    @Test
    void listsReportedCommentsWithReportCount() throws Exception {
        long c = comentar(carla, "comentario discutible");
        reportar(luis, c);
        reportar(ana, c);

        mvc.perform(getReq("/admin/comentarios/revision", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(c))
                .andExpect(jsonPath("$.content[0].ocultadoAutomaticamente").value(false))
                .andExpect(jsonPath("$.content[0].totalReportes").value(2))
                .andExpect(jsonPath("$.content[0].estado").value("PUBLICADO"));
    }

    @Test
    void filtersByType() throws Exception {
        long oculto = comentar(carla, "idiota");
        long reportado = comentar(carla, "dudoso");
        reportar(luis, reportado);

        mvc.perform(getReq("/admin/comentarios/revision?tipo=OCULTADOS", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oculto));
        mvc.perform(getReq("/admin/comentarios/revision?tipo=REPORTADOS", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(reportado));
        mvc.perform(getReq("/admin/comentarios/revision", admin)).andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void oldestPendingFirstAndPaginated() throws Exception {
        long a = comentar(carla, "idiota uno");
        long b = comentar(carla, "idiota dos");
        comentar(carla, "idiota tres");

        mvc.perform(getReq("/admin/comentarios/revision?size=2", admin))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].id").value(a))
                .andExpect(jsonPath("$.content[1].id").value(b));
    }

    @Test
    void deletedCommentsAreNotInTheQueue() throws Exception {
        long c = comentar(carla, "idiota");
        reportar(luis, comentar(carla, "otro"));
        mvc.perform(deleteReq("/comentarios/" + c, carla)).andExpect(status().isNoContent());

        mvc.perform(getReq("/admin/comentarios/revision", admin)).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void repliesAreAlsoReviewable() throws Exception {
        long raiz = comentar(carla, "raiz");
        long respuesta = cuerpo(mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", "idiota")))
                        .andReturn())
                .get("id")
                .asLong();

        mvc.perform(getReq("/admin/comentarios/revision", admin))
                .andExpect(jsonPath("$.content[0].id").value(respuesta))
                .andExpect(jsonPath("$.content[0].padreId").value(raiz));
    }

    @Test
    void onlyAdminsCanAccess() throws Exception {
        mvc.perform(getReq("/admin/comentarios/revision", carla)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/comentarios/revision")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidTypeAndPagination() throws Exception {
        mvc.perform(getReq("/admin/comentarios/revision?tipo=OTRO", admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq("/admin/comentarios/revision?size=0", admin)).andExpect(status().isBadRequest());
    }
}
