package co.edu.atlas.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestClockConfig.class)
class MediaControllerTests extends ApiIntegrationTest {

    @Autowired
    MutableClock clock;

    @Autowired
    MediaRepository medias;

    @Autowired
    AlmacenamientoMedia almacenamiento;

    Usuario admin;
    long ficha;

    @BeforeEach
    void preparar() throws Exception {
        admin = admin();
        ficha = crearBorrador(admin, fichaMinima("Con medios"));
    }

    private Map<String, Object> solicitud(String tipo, String rol, String contentType, long tamano) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tipo", tipo);
        if (rol != null) {
            m.put("rol", rol);
        }
        m.put("nombreArchivo", "archivo");
        m.put("contentType", contentType);
        m.put("tamanoBytes", tamano);
        return m;
    }

    private MvcResult subida(long fichaId, Map<String, Object> solicitud) throws Exception {
        return mvc.perform(jsonDe(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/admin/ejercicios/" + fichaId + "/media/subida"), admin, solicitud))
                .andReturn();
    }

    private long subirImagen() throws Exception {
        MvcResult r = subida(ficha, solicitud("IMAGEN", "POSICION_INICIAL", "image/png", 1000));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        return cuerpo(r).get("mediaId").asLong();
    }

    private void confirmar(long mediaId) throws Exception {
        mvc.perform(postReq("/admin/ejercicios/" + ficha + "/media/" + mediaId + "/confirmar", admin))
                .andExpect(status().isOk());
    }

    @Test
    void issuesPresignedUploadForAnImage() throws Exception {
        mvc.perform(jsonDe(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/admin/ejercicios/" + ficha + "/media/subida"),
                        admin, solicitud("IMAGEN", "POSICION_FINAL", "image/webp", 2048)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaId").isNumber())
                .andExpect(jsonPath("$.urlSubida").isNotEmpty())
                .andExpect(jsonPath("$.metodo").value("PUT"))
                .andExpect(jsonPath("$.cabeceras['Content-Type']").value("image/webp"))
                .andExpect(jsonPath("$.expiraEn").isNotEmpty());
    }

    @Test
    void acceptsGifAndMp4AsVideo() throws Exception {
        assertThat(subida(ficha, solicitud("VIDEO", null, "image/gif", 5000)).getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void rejectsUnsupportedFormatAndOversize() throws Exception {
        MvcResult tipo = subida(ficha, solicitud("VIDEO", null, "application/pdf", 10));
        assertThat(tipo.getResponse().getStatus()).isEqualTo(400);
        assertThat(cuerpo(tipo).get("code").asText()).isEqualTo("MEDIA_TIPO_NO_SOPORTADO");

        MvcResult grande = subida(ficha, solicitud("IMAGEN", "OTRO", "image/png", 5_242_881));
        assertThat(grande.getResponse().getStatus()).isEqualTo(400);
        assertThat(cuerpo(grande).get("code").asText()).isEqualTo("MEDIA_DEMASIADO_GRANDE");
    }

    @Test
    void imageRequiresRole() throws Exception {
        MvcResult r = subida(ficha, solicitud("IMAGEN", null, "image/png", 10));

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(cuerpo(r).get("code").asText()).isEqualTo("MEDIA_ROL_REQUERIDO");
    }

    @Test
    void validatesRequestBody() throws Exception {
        Map<String, Object> mala = new LinkedHashMap<>();
        mala.put("tipo", "IMAGEN");

        MvcResult r = subida(ficha, mala);

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(cuerpo(r).get("code").asText()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void unconfirmedMediaIsNotListed() throws Exception {
        subirImagen();

        mvc.perform(getReq("/admin/ejercicios/" + ficha, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").isEmpty());
    }

    @Test
    void confirmedMediaAppearsVideoFirst() throws Exception {
        long imagen = subirImagen();
        MvcResult v = subida(ficha, solicitud("VIDEO", null, "video/mp4", 9000));
        long video = cuerpo(v).get("mediaId").asLong();
        confirmar(imagen);
        confirmar(video);

        mvc.perform(getReq("/admin/ejercicios/" + ficha, admin))
                .andExpect(jsonPath("$.media.length()").value(2))
                .andExpect(jsonPath("$.media[0].tipo").value("VIDEO"))
                .andExpect(jsonPath("$.media[0].rol").doesNotExist())
                .andExpect(jsonPath("$.media[0].estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.media[0].url").isNotEmpty())
                .andExpect(jsonPath("$.media[1].tipo").value("IMAGEN"))
                .andExpect(jsonPath("$.media[1].rol").value("POSICION_INICIAL"))
                .andExpect(jsonPath("$.media[1].contentType").value("image/png"))
                .andExpect(jsonPath("$.media[1].tamanoBytes").value(1000));
    }

    @Test
    void confirmIsIdempotent() throws Exception {
        long id = subirImagen();
        confirmar(id);
        confirmar(id);
    }

    @Test
    void confirmFailsWhenObjectWasNeverUploaded() throws Exception {
        long id = subirImagen();
        almacenamiento.eliminar(medias.findById(id).orElseThrow().getClave());

        mvc.perform(postReq("/admin/ejercicios/" + ficha + "/media/" + id + "/confirmar", admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEDIA_NO_SUBIDA"));
    }

    @Test
    void confirmFailsAndDiscardsWhenStoredSizeDiffers() throws Exception {
        long id = subirImagen();
        String clave = medias.findById(id).orElseThrow().getClave();
        ((AlmacenamientoFake) almacenamiento).simularTamanoReal(clave, 999_999);

        mvc.perform(postReq("/admin/ejercicios/" + ficha + "/media/" + id + "/confirmar", admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEDIA_TAMANO_INVALIDO"));
        assertThat(medias.findById(id)).isEmpty();
        assertThat(almacenamiento.informacion(clave)).isEmpty();
    }

    @Test
    void enforcesImageLimitIncludingFreshPendingUploads() throws Exception {
        for (int i = 0; i < 8; i++) {
            subirImagen();
        }

        MvcResult r = subida(ficha, solicitud("IMAGEN", "OTRO", "image/png", 10));
        assertThat(r.getResponse().getStatus()).isEqualTo(409);
        assertThat(cuerpo(r).get("code").asText()).isEqualTo("MEDIA_LIMITE_ALCANZADO");
    }

    @Test
    void stalePendingUploadsDoNotCountTowardsTheLimit() throws Exception {
        for (int i = 0; i < 8; i++) {
            subirImagen();
        }
        clock.advance(Duration.ofMinutes(11));

        assertThat(subida(ficha, solicitud("IMAGEN", "OTRO", "image/png", 10)).getResponse().getStatus())
                .isEqualTo(201);
    }

    @Test
    void onlyOneVideoPerFicha() throws Exception {
        assertThat(subida(ficha, solicitud("VIDEO", null, "video/mp4", 10)).getResponse().getStatus()).isEqualTo(201);

        MvcResult segundo = subida(ficha, solicitud("VIDEO", null, "video/webm", 10));
        assertThat(segundo.getResponse().getStatus()).isEqualTo(409);
        assertThat(cuerpo(segundo).get("code").asText()).isEqualTo("MEDIA_LIMITE_ALCANZADO");
    }

    @Test
    void deleteRemovesRowAndStoredObject() throws Exception {
        long id = subirImagen();
        confirmar(id);
        String clave = medias.findById(id).orElseThrow().getClave();

        mvc.perform(deleteReq("/admin/ejercicios/" + ficha + "/media/" + id, admin)).andExpect(status().isNoContent());

        assertThat(medias.findById(id)).isEmpty();
        assertThat(almacenamiento.informacion(clave)).isEmpty();
        mvc.perform(deleteReq("/admin/ejercicios/" + ficha + "/media/" + id, admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEDIA_NO_ENCONTRADA"));
    }

    @Test
    void mediaOfAnotherFichaIsNotReachable() throws Exception {
        long id = subirImagen();
        long otra = crearBorrador(admin, fichaMinima("Otra"));

        mvc.perform(postReq("/admin/ejercicios/" + otra + "/media/" + id + "/confirmar", admin))
                .andExpect(status().isNotFound());
        mvc.perform(deleteReq("/admin/ejercicios/" + otra + "/media/" + id, admin)).andExpect(status().isNotFound());
    }

    @Test
    void unknownFichaIs404AndNonAdminForbidden() throws Exception {
        assertThat(subida(99999, solicitud("IMAGEN", "OTRO", "image/png", 10)).getResponse().getStatus())
                .isEqualTo(404);

        mvc.perform(postReq("/admin/ejercicios/" + ficha + "/media/1/confirmar", usuario()))
                .andExpect(status().isForbidden());
    }
}
