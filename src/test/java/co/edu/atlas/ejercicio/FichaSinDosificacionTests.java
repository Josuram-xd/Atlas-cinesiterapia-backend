package co.edu.atlas.ejercicio;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Fichas recargadas de BD sin ningun dato de dosificacion (Hibernate deja el embebido en null). */
class FichaSinDosificacionTests extends ApiIntegrationTest {

    @Autowired
    EntityManager em;

    private long crearBorrador(Usuario admin) throws Exception {
        long id = cuerpo(mvc.perform(jsonDe(post("/admin/ejercicios"), admin, fichaMinima("Sin dosis")))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
        em.flush();
        em.clear(); // fuerza a releer la ficha desde la BD, como en una peticion real
        return id;
    }

    @Test
    void adminGetOfDraftWithoutDosificacionReturnsNullBlock() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin);

        mvc.perform(autenticado(get("/admin/ejercicios/" + id), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sin dosis"))
                .andExpect(jsonPath("$.dosificacion").value(nullValue()))
                .andExpect(jsonPath("$.estudiantesCreadores").isEmpty())
                .andExpect(jsonPath("$.progresion").doesNotExist());
    }

    @Test
    void adminUpdateOfDraftWithoutDosificacionWorks() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin);
        Map<String, Object> cambio = fichaMinima("Sin dosis editada");
        cambio.put("version", 0);

        mvc.perform(jsonDe(put("/admin/ejercicios/" + id), admin, cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sin dosis editada"));
    }

    @Test
    void partialDosificacionKeepsObjectWithNullMissingFields() throws Exception {
        Usuario admin = admin();
        Map<String, Object> ficha = fichaMinima("Dosis parcial");
        ficha.put("dosificacion", Map.of("series", "3"));
        long id = cuerpo(mvc.perform(jsonDe(post("/admin/ejercicios"), admin, ficha)).andReturn())
                .get("id")
                .asLong();
        em.flush();
        em.clear();

        mvc.perform(autenticado(get("/admin/ejercicios/" + id), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dosificacion.series").value("3"))
                .andExpect(jsonPath("$.dosificacion.repeticiones").doesNotExist());
    }
}
