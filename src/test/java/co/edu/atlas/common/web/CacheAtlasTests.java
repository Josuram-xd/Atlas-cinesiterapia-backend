package co.edu.atlas.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

/** Cabeceras de cache para que el front (y su service worker) revalide con If-None-Match. */
class CacheAtlasTests extends ApiIntegrationTest {

    private String etag(MvcResult r) {
        return r.getResponse().getHeader(HttpHeaders.ETAG);
    }

    @Test
    void catalogIsCachedForAnHourPrivatelyWithEtag() throws Exception {
        Usuario u = usuario();

        MvcResult r = mvc.perform(getReq("/regiones", u))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=3600, private"))
                .andExpect(header().stringValues(HttpHeaders.VARY, org.hamcrest.Matchers.hasItem("Authorization")))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andReturn();

        mvc.perform(getReq("/regiones", u).header(HttpHeaders.IF_NONE_MATCH, etag(r)))
                .andExpect(status().isNotModified());
    }

    @Test
    void everyCatalogEndpointIsCacheable() throws Exception {
        Usuario u = usuario();
        for (String url : new String[] {"/categorias", "/regiones/" + regionId("anterior-rodilla") + "/categorias"}) {
            mvc.perform(getReq(url, u))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=3600, private"))
                    .andExpect(header().exists(HttpHeaders.ETAG));
        }
    }

    @Test
    void atlasListingsMustRevalidateAndChangeEtagWhenContentChanges() throws Exception {
        Usuario admin = admin();
        Usuario lector = usuario();
        publicada(admin, "Primera", "anterior-rodilla", 1);

        MvcResult antes = mvc.perform(getReq("/ejercicios", lector))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=0, must-revalidate, private"))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andReturn();
        mvc.perform(getReq("/ejercicios", lector).header(HttpHeaders.IF_NONE_MATCH, etag(antes)))
                .andExpect(status().isNotModified());

        publicada(admin, "Segunda", "anterior-rodilla", 1);

        MvcResult despues = mvc.perform(getReq("/ejercicios", lector).header(HttpHeaders.IF_NONE_MATCH, etag(antes)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(etag(despues)).isNotEqualTo(etag(antes));
    }

    @Test
    void sheetAndSearchHaveEtagAndRevalidate() throws Exception {
        Usuario admin = admin();
        Usuario lector = usuario();
        long id = publicada(admin, "Con etag", "anterior-rodilla", 1);

        MvcResult ficha = mvc.perform(getReq("/ejercicios/" + id, lector))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=0, must-revalidate, private"))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andReturn();
        mvc.perform(getReq("/ejercicios/" + id, lector).header(HttpHeaders.IF_NONE_MATCH, etag(ficha)))
                .andExpect(status().isNotModified());

        mvc.perform(getReq("/ejercicios/buscar", lector).param("q", "etag"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=0, must-revalidate, private"))
                .andExpect(header().exists(HttpHeaders.ETAG));
    }

    @Test
    void editingASheetInvalidatesItsEtag() throws Exception {
        Usuario admin = admin();
        Usuario lector = usuario();
        long id = publicada(admin, "Cambiante", "anterior-rodilla", 1);
        MvcResult antes = mvc.perform(getReq("/ejercicios/" + id, lector)).andReturn();

        var cambio = fichaCompleta("Cambiante");
        cambio.put("indicaciones", "Indicacion nueva");
        mvc.perform(putJson("/admin/ejercicios/" + id, admin, cambio)).andExpect(status().isOk());

        mvc.perform(getReq("/ejercicios/" + id, lector).header(HttpHeaders.IF_NONE_MATCH, etag(antes)))
                .andExpect(status().isOk());
    }

    @Test
    void errorsAreNeverCached() throws Exception {
        Usuario u = usuario();

        mvc.perform(getReq("/ejercicios/99999", u))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG));
        mvc.perform(getReq("/regiones/99999/categorias", u))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")));
        mvc.perform(get("/regiones"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.ETAG));
    }

    @Test
    void adminAndAuthResponsesAreNotCacheable() throws Exception {
        Usuario admin = admin();

        mvc.perform(getReq("/admin/ejercicios", admin))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG));
        mvc.perform(getReq("/auth/me", admin))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG));
    }
}
