package co.edu.atlas.comentario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.comentario.filtro.PalabraOfensivaRepository;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PalabraOfensivaAdminTests extends ApiIntegrationTest {

    static final String URL = "/admin/palabras-ofensivas";

    @Autowired
    PalabraOfensivaRepository repo;

    Usuario admin;
    Usuario carla;

    @BeforeEach
    void preparar() {
        admin = admin();
        carla = usuario();
    }

    long crear(String palabra) throws Exception {
        return cuerpo(mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", palabra)))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void listsSeedWordsPaginatedAndAlphabetically() throws Exception {
        mvc.perform(getReq(URL + "?size=5", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(repo.count()))
                .andExpect(jsonPath("$.content[0].palabra").isString());
    }

    @Test
    void filtersByText() throws Exception {
        mvc.perform(getReq(URL + "?q=MIÉRD", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].palabra").value("mierda"));
    }

    @Test
    void createsAWordAndTheFilterUsesItRightAway() throws Exception {
        crear("Chirimoya");
        long ficha = crearPublicada(admin, "Ficha");

        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "qué ch1r1m0y4")))
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));
    }

    @Test
    void storedWordIsKeptAsTypedAndNormalizedInternally() throws Exception {
        long id = crear("  Ñandú ");

        assertThat(repo.findById(id).orElseThrow().getPalabra()).isEqualTo("Ñandú");
        assertThat(repo.findById(id).orElseThrow().getPalabraNormalizada()).isEqualTo("nandu");
    }

    @Test
    void rejectsDuplicatesByNormalizedForm() throws Exception {
        mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", "CABRÓN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PALABRA_DUPLICADA"));
        mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", "m13rd4")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsBlankTooShortAndTooLongWords() throws Exception {
        mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", "  "))).andExpect(status().isBadRequest());
        mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", "a".repeat(61)))).andExpect(status().isBadRequest());
        mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", "ab")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PALABRA_INVALIDA"));
        mvc.perform(jsonDe(post(URL), admin, Map.of("palabra", "***")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PALABRA_INVALIDA"));
    }

    @Test
    void updatesAWord() throws Exception {
        long id = crear("Zanahoria");

        mvc.perform(putJson(URL + "/" + id, admin, Map.of("palabra", "Berenjena")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabra").value("Berenjena"));
        assertThat(repo.findById(id).orElseThrow().getPalabraNormalizada()).isEqualTo("berenjena");
    }

    @Test
    void updateKeepingItsOwnNormalizedFormIsAllowedButCollidingWithAnotherIsNot() throws Exception {
        long id = crear("Zanahoria");
        mvc.perform(putJson(URL + "/" + id, admin, Map.of("palabra", "ZANAHORIA"))).andExpect(status().isOk());

        mvc.perform(putJson(URL + "/" + id, admin, Map.of("palabra", "mierda")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PALABRA_DUPLICADA"));
    }

    @Test
    void deletesAWordAndTheFilterStopsUsingIt() throws Exception {
        long id = crear("Chirimoya");
        mvc.perform(deleteReq(URL + "/" + id, admin)).andExpect(status().isNoContent());
        long ficha = crearPublicada(admin, "Ficha");

        mvc.perform(jsonDe(post("/ejercicios/" + ficha + "/comentarios"), carla, Map.of("texto", "chirimoya")))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));
    }

    @Test
    void unknownWordIsNotFound() throws Exception {
        mvc.perform(deleteReq(URL + "/999999", admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PALABRA_NO_ENCONTRADA"));
        mvc.perform(putJson(URL + "/999999", admin, Map.of("palabra", "algo")))
                .andExpect(status().isNotFound());
    }

    @Test
    void onlyAdminsCanManageTheList() throws Exception {
        mvc.perform(getReq(URL, carla)).andExpect(status().isForbidden());
        mvc.perform(jsonDe(post(URL), carla, Map.of("palabra", "algo"))).andExpect(status().isForbidden());
        mvc.perform(deleteReq(URL + "/1", carla)).andExpect(status().isForbidden());
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mvc.perform(delete(URL + "/1")).andExpect(status().isUnauthorized());
    }
}
