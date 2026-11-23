package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.comentario.filtro.FiltroOfensivoService;
import co.edu.atlas.comentario.filtro.PalabraOfensivaRepository;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FiltroComentariosTests extends ApiIntegrationTest {

    @Autowired
    PalabraOfensivaRepository palabras;

    @Autowired
    FiltroOfensivoService filtro;

    Usuario carla;
    Usuario luis;
    long ficha;

    @BeforeEach
    void preparar() throws Exception {
        carla = usuario();
        luis = usuario();
        ficha = crearPublicada(admin(), "Ficha");
    }

    long comentar(Usuario u, String texto) throws Exception {
        return cuerpo(mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), u, Map.of("texto", texto)))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void initialListIsSeededAndSmall() {
        assertThat(palabras.count()).isBetween(10L, 40L);
        assertThat(palabras.existsByPalabraNormalizada("mierda")).isTrue();
    }

    @Test
    void cleanCommentIsPublishedImmediately() throws Exception {
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "Excelente ejercicio")))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.enRevision").value(false));
    }

    @Test
    void offensiveCommentIsHiddenFromOthersButShownToItsAuthorAsPending() throws Exception {
        long id = comentar(carla, "Eres una m13rd4");

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", carla))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].estado").value("EN_REVISION"))
                .andExpect(jsonPath("$.content[0].enRevision").value(true))
                .andExpect(jsonPath("$.content[0].propio").value(true));
    }

    @Test
    void creationResponseAlreadyFlagsPendingReview() throws Exception {
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "p.u.t.a")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"))
                .andExpect(jsonPath("$.enRevision").value(true));
    }

    @Test
    void offensiveEditHidesAPublishedComment() throws Exception {
        long id = comentar(carla, "Buen ejercicio");

        mvc.perform(putJson("/comentarios/" + id, carla, Map.of("texto", "ESTÚPIDO ejercicio")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void cleanEditRepublishesAPendingComment() throws Exception {
        long id = comentar(carla, "eres idiota");

        mvc.perform(putJson("/comentarios/" + id, carla, Map.of("texto", "perdón, buen ejercicio")))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis)).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void offensiveReplyIsHiddenLikeANewComment() throws Exception {
        long raiz = comentar(carla, "Buen ejercicio");
        mvc.perform(jsonDe(post("/comentarios/" + raiz + "/respuestas"), luis, Map.of("texto", "no seas imbécil")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", carla))
                .andExpect(jsonPath("$.content[0].respuestas.length()").value(0));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.content[0].respuestas.length()").value(1))
                .andExpect(jsonPath("$.content[0].respuestas[0].enRevision").value(true));
    }

    @Test
    void wordListChangesApplyImmediately() throws Exception {
        palabras.save(new co.edu.atlas.comentario.filtro.PalabraOfensiva("Chirimoya"));

        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "ch1r1m0y4")))
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
    }

    @Test
    void estadoParaUsesTheStoredListOnly() {
        PalabraOfensivaRepository repo = mock(PalabraOfensivaRepository.class);
        when(repo.todasNormalizadas()).thenReturn(List.of("tonto"));
        FiltroOfensivoService f = new FiltroOfensivoService(repo);

        assertThat(f.estadoPara("qué T0NT0")).isEqualTo(EstadoComentario.EN_REVISION);
        assertThat(f.estadoPara("mierda")).isEqualTo(EstadoComentario.PUBLICADO);
    }
}
