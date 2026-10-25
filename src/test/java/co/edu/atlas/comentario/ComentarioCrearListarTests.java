package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComentarioCrearListarTests extends ApiIntegrationTest {

    Usuario admin;
    Usuario carla;
    Usuario luis;
    long ficha;

    @BeforeEach
    void preparar() throws Exception {
        admin = admin();
        carla = usuario();
        luis = usuario();
        ficha = crearPublicada(admin, "Flexión de rodilla");
    }

    long comentar(Usuario u, long ejercicio, String texto) throws Exception {
        return cuerpo(mvc.perform(jsonDe(post("/ejercicios/" + ejercicio + "/comentarios"), u, Map.of("texto", texto)))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void publishesCommentAssociatedToSheetAndAuthor() throws Exception {
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "  Muy útil  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ejercicioId").value(ficha))
                .andExpect(jsonPath("$.texto").value("Muy útil"))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.enRevision").value(false))
                .andExpect(jsonPath("$.propio").value(true))
                .andExpect(jsonPath("$.autor.id").value(carla.getId()))
                .andExpect(jsonPath("$.autor.rolPerfil").value("PROFESIONAL"))
                .andExpect(jsonPath("$.respuestas.length()").value(0));
    }

    @Test
    void doesNotExposeAuthorEmail() throws Exception {
        long id = comentar(carla, ficha, "hola");

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].autor.correo").doesNotExist())
                .andExpect(jsonPath("$.content[0].autor.nombre").value(carla.getCorreo().split("@")[0]))
                .andExpect(jsonPath("$.content[0].propio").value(false));
    }

    @Test
    void listsNewestFirstWithPagination() throws Exception {
        long a = comentar(carla, ficha, "uno");
        long b = comentar(luis, ficha, "dos");
        long c = comentar(carla, ficha, "tres");

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios?size=2", luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].id").value(c))
                .andExpect(jsonPath("$.content[1].id").value(b));
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios?size=2&page=1", luis))
                .andExpect(jsonPath("$.content[0].id").value(a));
    }

    @Test
    void sheetWithoutCommentsReturnsEmptyPage() throws Exception {
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void commentsOfOtherSheetsAreNotMixed() throws Exception {
        long otra = crearPublicada(admin, "Otra ficha");
        comentar(carla, otra, "de otra");

        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/ejercicios/" + ficha + "/comentarios")).andExpect(status().isUnauthorized());
        mvc.perform(post("/ejercicios/" + ficha + "/comentarios")
                        .contentType("application/json")
                        .content("{\"texto\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void draftOrMissingSheetIsNotFound() throws Exception {
        long borrador = crearBorrador(admin, fichaMinima("Borrador"));

        mvc.perform(jsonDe(post("/ejercicios/" + borrador + "/comentarios"), carla, Map.of("texto", "x")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EJERCICIO_NO_ENCONTRADO"));
        mvc.perform(getReq("/ejercicios/999999/comentarios", carla)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsBlankTooLongAndMarkupText() throws Exception {
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "a".repeat(1001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "<script>alert(1)</script>")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEXTO_INVALIDO"));
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios?size=101", carla)).andExpect(status().isBadRequest());
        mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios?page=-1", carla)).andExpect(status().isBadRequest());
    }

    @Test
    void personalizedResponsesAreNotCacheable() throws Exception {
        comentar(carla, ficha, "hola");

        String cache = mvc.perform(getReq("/ejercicios/" + ficha + "/comentarios", luis))
                .andExpect(status().isOk())
                .andExpect(header().exists("Cache-Control"))
                .andReturn()
                .getResponse()
                .getHeader("Cache-Control");
        assertThat(cache).contains("no-store");
    }
}
