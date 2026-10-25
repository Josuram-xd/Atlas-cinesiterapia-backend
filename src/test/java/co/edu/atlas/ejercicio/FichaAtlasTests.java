package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FichaAtlasTests extends ApiIntegrationTest {

    Usuario admin;
    Usuario lector;

    @BeforeEach
    void preparar() {
        admin = admin();
        lector = usuario();
    }

    // ---- 3.3 ficha completa ----

    @Test
    void returnsTheFullSheetWithMediaSplitIntoVideoAndImages() throws Exception {
        long id = publicada(admin, "Ficha completa", "anterior-rodilla", 1);
        Map<String, Object> video = new LinkedHashMap<>();
        video.put("tipo", "VIDEO");
        video.put("contentType", "video/mp4");
        video.put("tamanoBytes", 5000);
        long videoId = cuerpo(mvc.perform(jsonDe(post("/admin/ejercicios/" + id + "/media/subida"), admin, video))
                        .andReturn())
                .get("mediaId")
                .asLong();
        mvc.perform(postReq("/admin/ejercicios/" + id + "/media/" + videoId + "/confirmar", admin)).andReturn();

        mvc.perform(getReq("/ejercicios/" + id, lector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Ficha completa"))
                .andExpect(jsonPath("$.region.vista").value("ANTERIOR"))
                .andExpect(jsonPath("$.region.svgId").value("anterior-rodilla"))
                .andExpect(jsonPath("$.categoria.nombre").value("Movilización pasiva"))
                .andExpect(jsonPath("$.objetivoTerapeutico").value("Recuperar el rango de flexión"))
                .andExpect(jsonPath("$.precaucionesContraindicaciones").value("Dolor agudo, inflamación severa"))
                .andExpect(jsonPath("$.posicionPaciente").value("Decúbito supino"))
                .andExpect(jsonPath("$.comandosVerbales").value("Dobla despacio, mantén, vuelve"))
                .andExpect(jsonPath("$.dosificacion.series").value("3"))
                .andExpect(jsonPath("$.dosificacion.frecuencia").value("Diaria"))
                .andExpect(jsonPath("$.progresion").doesNotExist())
                .andExpect(jsonPath("$.estudiantesCreadores.length()").value(2))
                .andExpect(jsonPath("$.cohorte").value("2025-1"))
                .andExpect(jsonPath("$.profesoraRevisora").value("Dra. Martínez"))
                .andExpect(jsonPath("$.video.id").value(videoId))
                .andExpect(jsonPath("$.video.url").isNotEmpty())
                .andExpect(jsonPath("$.imagenes.length()").value(1))
                .andExpect(jsonPath("$.imagenes[0].rol").value("POSICION_INICIAL"))
                .andExpect(jsonPath("$.imagenes[0].contentType").value("image/png"))
                .andExpect(jsonPath("$.estado").doesNotExist())
                .andExpect(jsonPath("$.creadoPor").doesNotExist())
                .andExpect(jsonPath("$.camposFaltantes").doesNotExist())
                .andExpect(jsonPath("$.actualizadoEn").isNotEmpty());
    }

    @Test
    void sheetWithoutVideoHasNullVideo() throws Exception {
        long id = publicada(admin, "Sin video", "anterior-rodilla", 1);

        mvc.perform(getReq("/ejercicios/" + id, lector))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.video").value((Object) null));
    }

    @Test
    void nonPublishedSheetsAreNotFound() throws Exception {
        long borrador = crearBorrador(admin, fichaMinima("Borrador"));
        long archivada = publicada(admin, "Archivada", "anterior-rodilla", 1);
        mvc.perform(postReq("/admin/ejercicios/" + archivada + "/archivar", admin)).andExpect(status().isOk());
        long eliminada = publicada(admin, "Eliminada", "anterior-rodilla", 1);
        mvc.perform(deleteReq("/admin/ejercicios/" + eliminada, admin)).andExpect(status().isNoContent());

        for (long id : new long[] {borrador, archivada, eliminada, 99999}) {
            mvc.perform(getReq("/ejercicios/" + id, lector))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EJERCICIO_NO_ENCONTRADO"));
        }
        mvc.perform(get("/ejercicios/" + borrador)).andExpect(status().isUnauthorized());
    }

    @Test
    void adminsReadingTheAtlasAlsoOnlySeePublished() throws Exception {
        long borrador = crearBorrador(admin, fichaMinima("Borrador"));

        mvc.perform(getReq("/ejercicios/" + borrador, admin)).andExpect(status().isNotFound());
    }
}
