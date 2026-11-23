package co.edu.atlas.comentario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ComentarioEditarEliminarTests extends ApiIntegrationTest {

    @Autowired
    ComentarioRepository repo;

    Usuario carla;
    Usuario luis;
    long ficha;
    long comentario;

    @BeforeEach
    void preparar() throws Exception {
        Usuario admin = admin();
        carla = usuario();
        luis = usuario();
        ficha = crearPublicada(admin, "Ficha");
        comentario = cuerpo(mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "original")))
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void authorEditsOwnCommentAndItIsMarkedAsEdited() throws Exception {
        mvc.perform(putJson("/comentarios/" + comentario, carla, Map.of("texto", " corregido ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("corregido"))
                .andExpect(jsonPath("$.editadoAt").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));
    }

    @Test
    void otherUserCannotEditOrDelete() throws Exception {
        mvc.perform(putJson("/comentarios/" + comentario, luis, Map.of("texto", "hack")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_ES_AUTOR"));
        mvc.perform(deleteReq("/comentarios/" + comentario, luis))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NO_ES_AUTOR"));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.content[0].texto").value("original"));
    }

    @Test
    void adminCannotEditSomeoneElsesCommentThroughTheUserEndpoint() throws Exception {
        mvc.perform(putJson("/comentarios/" + comentario, admin(), Map.of("texto", "x")))
                .andExpect(status().isForbidden());
    }

    @Test
    void editRejectsInvalidTextLikeCreation() throws Exception {
        mvc.perform(putJson("/comentarios/" + comentario, carla, Map.of("texto", "  ")))
                .andExpect(status().isBadRequest());
        mvc.perform(putJson("/comentarios/" + comentario, carla, Map.of("texto", "<script>x</script>")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEXTO_INVALIDO"));
    }

    @Test
    void authorDeletesOwnCommentAndItDisappears() throws Exception {
        mvc.perform(deleteReq("/comentarios/" + comentario, carla)).andExpect(status().isNoContent());

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.totalElements").value(0));
        Comentario c = repo.findById(comentario).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(c.getEstado()).isEqualTo(EstadoComentario.ELIMINADO);
        org.assertj.core.api.Assertions.assertThat(c.getTexto()).isEmpty();
    }

    @Test
    void deletedCommentWithRepliesShowsAsDeletedPlaceholder() throws Exception {
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), luis, Map.of("texto", "otro")));
        long raiz = comentario;
        // respuesta directa en base para no depender de 4.4
        repo.save(new Comentario(ficha, luis, raiz, "respuesta", EstadoComentario.PUBLICADO));
        mvc.perform(deleteReq("/comentarios/" + raiz, carla)).andExpect(status().isNoContent());

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[1].id").value(raiz))
                .andExpect(jsonPath("$.content[1].eliminado").value(true))
                .andExpect(jsonPath("$.content[1].texto").doesNotExist())
                .andExpect(jsonPath("$.content[1].autor").doesNotExist())
                .andExpect(jsonPath("$.content[1].respuestas[0].texto").value("respuesta"));
    }

    @Test
    void deletedOrUnknownCommentIsNotFound() throws Exception {
        mvc.perform(deleteReq("/comentarios/" + comentario, carla)).andExpect(status().isNoContent());

        mvc.perform(deleteReq("/comentarios/" + comentario, carla))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMENTARIO_NO_ENCONTRADO"));
        mvc.perform(putJson("/comentarios/" + comentario, carla, Map.of("texto", "x"))).andExpect(status().isNotFound());
        mvc.perform(deleteReq("/comentarios/999999", carla)).andExpect(status().isNotFound());
    }

    @Test
    void pendingCommentOfAnotherUserIsHiddenNotForbidden() throws Exception {
        Comentario oculto = repo.save(new Comentario(ficha, carla, null, "oculto", EstadoComentario.EN_REVISION));

        mvc.perform(deleteReq("/comentarios/" + oculto.getId(), luis)).andExpect(status().isNotFound());
        mvc.perform(deleteReq("/comentarios/" + oculto.getId(), carla)).andExpect(status().isNoContent());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(put("/comentarios/" + comentario).contentType("application/json").content("{\"texto\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/comentarios/" + comentario)).andExpect(status().isUnauthorized());
    }
}
