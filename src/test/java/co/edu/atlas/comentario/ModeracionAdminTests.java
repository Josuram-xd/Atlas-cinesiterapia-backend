package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ModeracionAdminTests extends ApiIntegrationTest {

    @Autowired
    ComentarioRepository comentarios;

    @Autowired
    ReporteComentarioRepository reportes;

    Usuario admin;
    Usuario carla;
    Usuario luis;
    long ficha;

    @BeforeEach
    void preparar() throws Exception {
        admin = admin();
        carla = usuario();
        luis = usuario();
        ficha = crearPublicada(admin, "Ficha");
    }

    long comentar(Usuario u, String texto) throws Exception {
        return cuerpo(mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), u, Map.of("texto", texto)))
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void acceptingAnAutoHiddenCommentPublishesItForEveryone() throws Exception {
        long id = comentar(carla, "eres idiota");

        mvc.perform(postReq("/admin/comentarios/" + id + "/aceptar", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.ocultadoAutomaticamente").value(false));

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis)).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", carla))
                .andExpect(jsonPath("$.content[0].enRevision").value(false));
        mvc.perform(getReq("/admin/comentarios/revision", admin)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void acceptingAReportedCommentClearsItsReports() throws Exception {
        long id = comentar(carla, "dudoso");
        mvc.perform(postReq("/comentarios/" + id + "/reportes", luis)).andExpect(status().isCreated());

        mvc.perform(postReq("/admin/comentarios/" + id + "/aceptar", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReportes").value(0));

        assertThat(reportes.countByComentarioId(id)).isZero();
        mvc.perform(getReq("/admin/comentarios/revision", admin)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis)).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void afterAcceptingTheSameUserMayReportAgain() throws Exception {
        long id = comentar(carla, "dudoso");
        mvc.perform(postReq("/comentarios/" + id + "/reportes", luis)).andExpect(status().isCreated());
        mvc.perform(postReq("/admin/comentarios/" + id + "/aceptar", admin)).andExpect(status().isOk());

        mvc.perform(postReq("/comentarios/" + id + "/reportes", luis)).andExpect(status().isCreated());
    }

    @Test
    void adminCanDeleteAPublishedComment() throws Exception {
        long id = comentar(carla, "publicado");

        mvc.perform(deleteReq("/admin/comentarios/" + id, admin)).andExpect(status().isNoContent());

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis)).andExpect(jsonPath("$.totalElements").value(0));
        assertThat(comentarios.findById(id).orElseThrow().getEstado()).isEqualTo(EstadoComentario.ELIMINADO);
    }

    @Test
    void adminDeletingFromTheQueueRemovesItEverywhere() throws Exception {
        long oculto = comentar(carla, "idiota");
        long reportado = comentar(carla, "dudoso");
        mvc.perform(postReq("/comentarios/" + reportado + "/reportes", luis)).andExpect(status().isCreated());

        mvc.perform(deleteReq("/admin/comentarios/" + oculto, admin)).andExpect(status().isNoContent());
        mvc.perform(deleteReq("/admin/comentarios/" + reportado, admin)).andExpect(status().isNoContent());

        mvc.perform(getReq("/admin/comentarios/revision", admin)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", carla)).andExpect(jsonPath("$.totalElements").value(0));
        assertThat(reportes.countByComentarioId(reportado)).isZero();
    }

    @Test
    void deletedRootWithRepliesKeepsThePlaceholder() throws Exception {
        long raiz = comentar(carla, "raiz");
        mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", "respuesta")))
                .andExpect(status().isCreated());

        mvc.perform(deleteReq("/admin/comentarios/" + raiz, admin)).andExpect(status().isNoContent());

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.content[0].eliminado").value(true))
                .andExpect(jsonPath("$.content[0].respuestas.length()").value(1));
    }

    @Test
    void deletedOrMissingCommentIsNotFound() throws Exception {
        long id = comentar(carla, "x");
        mvc.perform(deleteReq("/admin/comentarios/" + id, admin)).andExpect(status().isNoContent());

        mvc.perform(deleteReq("/admin/comentarios/" + id, admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMENTARIO_NO_ENCONTRADO"));
        mvc.perform(postReq("/admin/comentarios/" + id + "/aceptar", admin)).andExpect(status().isNotFound());
        mvc.perform(postReq("/admin/comentarios/999999/aceptar", admin)).andExpect(status().isNotFound());
    }

    @Test
    void acceptingAnAlreadyPublishedCommentIsIdempotent() throws Exception {
        long id = comentar(carla, "limpio");

        mvc.perform(postReq("/admin/comentarios/" + id + "/aceptar", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));
    }

    @Test
    void regularUsersCannotModerate() throws Exception {
        long id = comentar(carla, "x");

        mvc.perform(postReq("/admin/comentarios/" + id + "/aceptar", luis)).andExpect(status().isForbidden());
        mvc.perform(deleteReq("/admin/comentarios/" + id, carla)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/comentarios/" + id + "/aceptar")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/admin/comentarios/" + id)).andExpect(status().isUnauthorized());
        assertThat(comentarios.findById(id).orElseThrow().getEstado()).isEqualTo(EstadoComentario.PUBLICADO);
    }
}
