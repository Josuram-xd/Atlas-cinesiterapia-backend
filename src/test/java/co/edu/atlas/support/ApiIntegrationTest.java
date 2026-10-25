package co.edu.atlas.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** Base de los tests de integracion HTTP: usuarios con token, cuerpos JSON y atajos para fichas. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiIntegrationTest {

    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected UsuarioRepository usuarios;

    @Autowired
    protected JwtService jwt;

    @Autowired
    protected RegionAnatomicaRepository regiones;

    @Autowired
    protected CategoriaTerapeuticaRepository categorias;

    protected Usuario crearUsuario(RolSistema rol) {
        String correo = rol.name().toLowerCase() + SECUENCIA.incrementAndGet() + "@correo.com";
        return usuarios.save(new Usuario(correo, "hash", RolPerfil.PROFESIONAL, rol));
    }

    protected String bearer(Usuario u) {
        return "Bearer " + jwt.generarAccessToken(u);
    }

    protected Usuario admin() {
        return crearUsuario(RolSistema.ADMIN);
    }

    protected Usuario usuario() {
        return crearUsuario(RolSistema.USER);
    }

    protected long regionId(String svgId) {
        return regiones.findBySvgId(svgId).orElseThrow().getId();
    }

    protected long categoriaId(int orden) {
        return categorias.findAllByOrderByOrdenAsc().get(orden - 1).getId();
    }

    protected MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder req, Usuario u) {
        return req.header("Authorization", bearer(u));
    }

    protected MockHttpServletRequestBuilder jsonDe(MockHttpServletRequestBuilder req, Usuario u, Object cuerpo)
            throws Exception {
        return autenticado(req, u).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(cuerpo));
    }

    protected JsonNode cuerpo(MvcResult r) throws Exception {
        return mapper.readTree(r.getResponse().getContentAsString());
    }

    /** Solicitud minima valida (borrador). */
    protected Map<String, Object> fichaMinima(String nombre) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nombre", nombre);
        m.put("regionId", regionId("anterior-rodilla"));
        m.put("categoriaId", categoriaId(1));
        return m;
    }

    /** Solicitud con todos los campos obligatorios para publicar (sin medios). */
    protected Map<String, Object> fichaCompleta(String nombre) {
        Map<String, Object> m = fichaMinima(nombre);
        m.put("objetivoTerapeutico", "Recuperar el rango de flexión");
        m.put("estructurasIntervenidas", "Isquiotibiales, articulación de la rodilla");
        m.put("indicaciones", "Postoperatorio de rodilla");
        m.put("precaucionesContraindicaciones", "Dolor agudo, inflamación severa");
        m.put("posicionPaciente", "Decúbito supino");
        m.put("posicionFisioterapeuta", "De pie al costado, toma en tobillo");
        m.put("ejecucion", "1. Flexionar la rodilla. 2. Volver a la posición inicial.");
        m.put("comandosVerbales", "Dobla despacio, mantén, vuelve");
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("series", "3");
        d.put("repeticiones", "12");
        d.put("intensidad", "Baja");
        d.put("tiempo", "5 s");
        d.put("descanso", "30 s");
        d.put("frecuencia", "Diaria");
        m.put("dosificacion", d);
        m.put("estudiantesCreadores", List.of("Ana Gómez", "Luis Pérez"));
        m.put("cohorte", "2025-1");
        m.put("profesoraRevisora", "Dra. Martínez");
        return m;
    }

    protected long crearBorrador(Usuario admin, Map<String, Object> ficha) throws Exception {
        MvcResult r = mvc.perform(jsonDe(post("/admin/ejercicios"), admin, ficha)).andReturn();
        return cuerpo(r).get("id").asLong();
    }

    protected MockHttpServletRequestBuilder getReq(String url, Usuario u) {
        return autenticado(get(url), u);
    }

    protected MockHttpServletRequestBuilder postReq(String url, Usuario u) {
        return autenticado(post(url), u);
    }

    protected MockHttpServletRequestBuilder putJson(String url, Usuario u, Object cuerpo) throws Exception {
        return jsonDe(put(url), u, cuerpo);
    }

    protected MockHttpServletRequestBuilder deleteReq(String url, Usuario u) {
        return autenticado(delete(url), u);
    }
}
