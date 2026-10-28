package co.edu.atlas.comentario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ComentarioRespuestasTests extends ApiIntegrationTest {

    @Autowired
    ComentarioRepository repo;

    Usuario carla;
    Usuario luis;
    long ficha;
    long raiz;

    @BeforeEach
    void preparar() throws Exception {
        carla = usuario();
        luis = usuario();
        ficha = crearPublicada(admin(), "Ficha");
        raiz = comentar("/ejercicios/" + ficha + "/comentarios", carla, "raiz");
    }

    long comentar(String url, Usuario u, String texto) throws Exception {
        return cuerpo(mvc.perform(jsonDe(post(url), u, Map.of("texto", texto))).andExpect(status().isCreated()).andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void repliesToARootCommentFormAThread() throws Exception {
        mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", "Gracias")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.padreId").value(raiz))
                .andExpect(jsonPath("$.ejercicioId").value(ficha))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.respuestas.length()").value(0));
    }

    @Test
    void severalRepliesShareTheThreadInChronologicalOrder() throws Exception {
        long r1 = comentar("/comentarios/" + raiz + "/respuestas", luis, "uno");
        long r2 = comentar("/comentarios/" + raiz + "/respuestas", carla, "dos");

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].respuestas.length()").value(2))
                .andExpect(jsonPath("$.content[0].respuestas[0].id").value(r1))
                .andExpect(jsonPath("$.content[0].respuestas[1].id").value(r2))
                .andExpect(jsonPath("$.content[0].respuestas[0].respuestas.length()").value(0));
    }

    @Test
    void replyToAReplyIsFlattenedUnderTheRoot() throws Exception {
        long r1 = comentar("/comentarios/" + raiz + "/respuestas", luis, "uno");

        mvc.perform(jsonDe(post("/comentarios/" + r1 + "/respuestas"), carla, Map.of("texto", "respuesta a respuesta")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.padreId").value(raiz));
    }

    @Test
    void cannotReplyToDeletedOrMissingComment() throws Exception {
        Comentario c = repo.findById(raiz).orElseThrow();
        c.eliminar(java.time.Instant.now());
        repo.save(c);

        mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", "x")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMENTARIO_NO_ENCONTRADO"));
        mvc.perform(jsonDe(post("/comentarios/999999/respuestas"), luis, Map.of("texto", "x")))
                .andExpect(status().isNotFound());
    }

    @Test
    void pendingCommentsOfOthersAreInvisibleAndOwnPendingCannotBeRepliedTo() throws Exception {
        Comentario oculto = repo.save(new Comentario(ficha, carla, null, "oculto", EstadoComentario.EN_REVISION));

        mvc.perform(jsonDe(post("/comentarios/" + oculto.getId() + "/respuestas"), luis, Map.of("texto", "x")))
                .andExpect(status().isNotFound());
        mvc.perform(jsonDe(post("/comentarios/" + oculto.getId() + "/respuestas"), carla, Map.of("texto", "x")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMENTARIO_NO_RESPONDIBLE"));
    }

    @Test
    void replyValidatesAndSanitizesText() throws Exception {
        mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", " ")))
                .andExpect(status().isBadRequest());
        mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", "<b>x</b>")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEXTO_INVALIDO"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(post("/comentarios/" + raiz + "/respuestas").contentType("application/json").content("{\"texto\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }
}
