package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class EjercicioAdminControllerTests extends ApiIntegrationTest {

    @Test
    void createsMinimalDraft() throws Exception {
        Usuario admin = admin();

        mvc.perform(jsonDe(post("/admin/ejercicios"), admin, fichaMinima("  Flexión de rodilla ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.nombre").value("Flexión de rodilla"))
                .andExpect(jsonPath("$.region.svgId").value("anterior-rodilla"))
                .andExpect(jsonPath("$.region.vista").value("ANTERIOR"))
                .andExpect(jsonPath("$.categoria.nombre").value("Movilización pasiva"))
                .andExpect(jsonPath("$.creadoPor.id").value(admin.getId()))
                .andExpect(jsonPath("$.creadoPor.correo").value(admin.getCorreo()))
                .andExpect(jsonPath("$.dosificacion.series").doesNotExist())
                .andExpect(jsonPath("$.estudiantesCreadores").isEmpty());
    }

    @Test
    void storesAllContentAndNormalizesBlanks() throws Exception {
        Usuario admin = admin();
        Map<String, Object> ficha = fichaCompleta("Ejercicio completo");
        ficha.put("progresion", "   ");
        ficha.put("estudiantesCreadores", List.of(" Ana ", "", "Luis"));

        MvcResult r = mvc.perform(jsonDe(post("/admin/ejercicios"), admin, ficha))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.progresion").doesNotExist())
                .andExpect(jsonPath("$.estudiantesCreadores[0]").value("Ana"))
                .andExpect(jsonPath("$.estudiantesCreadores[1]").value("Luis"))
                .andExpect(jsonPath("$.dosificacion.frecuencia").value("Diaria"))
                .andExpect(jsonPath("$.cohorte").value("2025-1"))
                .andReturn();

        mvc.perform(getReq("/admin/ejercicios/" + cuerpo(r).get("id").asLong(), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivoTerapeutico").value("Recuperar el rango de flexión"));
    }

    @Test
    void rejectsMissingNameWithFieldError() throws Exception {
        Map<String, Object> ficha = fichaMinima("x");
        ficha.remove("nombre");

        mvc.perform(jsonDe(post("/admin/ejercicios"), admin(), ficha))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='nombre')]").exists());
    }

    @Test
    void rejectsMissingRegionAndCategory() throws Exception {
        mvc.perform(jsonDe(post("/admin/ejercicios"), admin(), Map.of("nombre", "Sin catalogos")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='regionId')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='categoriaId')]").exists());
    }

    @Test
    void rejectsMalformedCohortAndTooLongFields() throws Exception {
        Map<String, Object> ficha = fichaMinima("Valida");
        ficha.put("cohorte", "2025/1");
        ficha.put("profesoraRevisora", "x".repeat(151));

        mvc.perform(jsonDe(post("/admin/ejercicios"), admin(), ficha))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='cohorte')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='profesoraRevisora')]").exists());
    }

    @Test
    void rejectsUnknownRegionAndCategory() throws Exception {
        Map<String, Object> ficha = fichaMinima("Valida");
        ficha.put("regionId", 99999);
        mvc.perform(jsonDe(post("/admin/ejercicios"), admin(), ficha))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REGION_INVALIDA"));

        Map<String, Object> otra = fichaMinima("Valida");
        otra.put("categoriaId", 99999);
        mvc.perform(jsonDe(post("/admin/ejercicios"), admin(), otra))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CATEGORIA_INVALIDA"));
    }

    @Test
    void nonAdminsAreForbiddenAndAnonymousUnauthorized() throws Exception {
        mvc.perform(jsonDe(post("/admin/ejercicios"), usuario(), fichaMinima("x"))).andExpect(status().isForbidden());
        mvc.perform(getReq("/admin/ejercicios", usuario())).andExpect(status().isForbidden());
        mvc.perform(get("/admin/ejercicios/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void updatesDraftIncludingRegionAndClearingFields() throws Exception {
        Usuario admin = admin();
        long id = crearBorrador(admin, fichaCompleta("Original"));
        Map<String, Object> cambio = fichaMinima("Renombrado");
        cambio.put("regionId", regionId("posterior-lumbar"));
        cambio.put("indicaciones", "Nueva indicación");

        mvc.perform(putJson("/admin/ejercicios/" + id, admin, cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Renombrado"))
                .andExpect(jsonPath("$.region.svgId").value("posterior-lumbar"))
                .andExpect(jsonPath("$.indicaciones").value("Nueva indicación"))
                .andExpect(jsonPath("$.objetivoTerapeutico").doesNotExist())
                .andExpect(jsonPath("$.estudiantesCreadores").isEmpty())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.actualizadoPor.id").value(admin.getId()));
    }

    @Test
    void getAndUpdateOfMissingFichaAre404() throws Exception {
        Usuario admin = admin();
        mvc.perform(getReq("/admin/ejercicios/9999", admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EJERCICIO_NO_ENCONTRADO"));
        mvc.perform(putJson("/admin/ejercicios/9999", admin, fichaMinima("x"))).andExpect(status().isNotFound());
    }

    @Test
    void listsWithFiltersAndPagination() throws Exception {
        Usuario admin = admin();
        crearBorrador(admin, fichaMinima("Flexión de rodilla"));
        crearBorrador(admin, fichaMinima("Extensión de rodilla"));
        Map<String, Object> lumbar = fichaMinima("Puente lumbar");
        lumbar.put("regionId", regionId("posterior-lumbar"));
        crearBorrador(admin, lumbar);

        mvc.perform(getReq("/admin/ejercicios?size=2", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2));

        mvc.perform(getReq("/admin/ejercicios?q=FLEXION", admin))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Flexión de rodilla"))
                .andExpect(jsonPath("$.content[0].creadoPor.id").value(admin.getId()))
                .andExpect(jsonPath("$.content[0].estado").value("BORRADOR"));

        mvc.perform(getReq("/admin/ejercicios?regionId=" + regionId("posterior-lumbar"), admin))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(getReq("/admin/ejercicios?estado=ARCHIVADO", admin))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(getReq("/admin/ejercicios?categoriaId=" + categoriaId(2), admin))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void invalidListParametersAreRejected() throws Exception {
        Usuario admin = admin();
        mvc.perform(getReq("/admin/ejercicios?size=0", admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq("/admin/ejercicios?size=101", admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq("/admin/ejercicios?page=-1", admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq("/admin/ejercicios?estado=INVENTADO", admin)).andExpect(status().isBadRequest());
    }

    @Test
    void likeWildcardsInSearchAreLiteral() throws Exception {
        Usuario admin = admin();
        crearBorrador(admin, fichaMinima("Ejercicio al 100%"));
        crearBorrador(admin, fichaMinima("Otro ejercicio"));

        MvcResult r = mvc.perform(getReq("/admin/ejercicios", admin).param("q", "%")).andReturn();

        assertThat(cuerpo(r).get("totalElements").asInt()).isEqualTo(1);
    }
}
