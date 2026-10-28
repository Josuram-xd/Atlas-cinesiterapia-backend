package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReporteComentarioTests extends ApiIntegrationTest {

    @Autowired
    ReporteComentarioRepository reportes;

    @Autowired
    ComentarioRepository comentarios;

    Usuario carla;
    Usuario luis;
    Usuario ana;
    long ficha;
    long comentario;

    @BeforeEach
    void preparar() throws Exception {
        carla = usuario();
        luis = usuario();
        ana = usuario();
        ficha = crearPublicada(admin(), "Ficha");
        comentario = cuerpo(mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "hola")))
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void reportsAnotherUsersComment() throws Exception {
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", luis))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comentarioId").value(comentario));

        assertThat(reportes.countByComentarioId(comentario)).isEqualTo(1);
    }

    @Test
    void reportedCommentStaysVisibleWhileUnderReview() throws Exception {
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", luis)).andExpect(status().isCreated());

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", ana))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].estado").value("PUBLICADO"));
    }

    @Test
    void sameUserCannotReportTwice() throws Exception {
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", luis)).andExpect(status().isCreated());

        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", luis))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("YA_REPORTADO"));
        assertThat(reportes.countByComentarioId(comentario)).isEqualTo(1);
    }

    @Test
    void differentUsersAccumulateReports() throws Exception {
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", luis)).andExpect(status().isCreated());
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", ana)).andExpect(status().isCreated());

        assertThat(reportes.countByComentarioId(comentario)).isEqualTo(2);
    }

    @Test
    void cannotReportOwnComment() throws Exception {
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", carla))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("REPORTE_PROPIO"));
        assertThat(reportes.countByComentarioId(comentario)).isZero();
    }

    @Test
    void cannotReportInvisibleDeletedOrMissingComments() throws Exception {
        Comentario oculto = comentarios.save(new Comentario(ficha, carla, null, "oculto", EstadoComentario.EN_REVISION));
        mvc.perform(postReq("/comentarios/" + oculto.getId() + "/reportes", luis)).andExpect(status().isNotFound());

        mvc.perform(deleteReq("/comentarios/" + comentario, carla)).andExpect(status().isNoContent());
        mvc.perform(postReq("/comentarios/" + comentario + "/reportes", luis)).andExpect(status().isNotFound());
        mvc.perform(postReq("/comentarios/999999/reportes", luis))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMENTARIO_NO_ENCONTRADO"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(post("/comentarios/" + comentario + "/reportes")).andExpect(status().isUnauthorized());
    }
}
