package co.edu.atlas.metricas;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UsuariosActivosTests extends ApiIntegrationTest {

    static final String URL = "/admin/metricas/usuarios-activos";
    static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    VisitaRepository visitas;

    @Autowired
    SesionUsoRepository sesiones;

    Usuario admin;

    @BeforeEach
    void preparar() {
        admin = admin();
    }

    private Usuario conRol(RolPerfil rol) {
        return usuarios.save(new Usuario("rol" + SEQ.incrementAndGet() + "@correo.com", "hash", rol, RolSistema.USER));
    }

    private void visita(Usuario u, String instante) {
        visitas.save(new Visita(u.getId(), TipoVisita.ATLAS, null, Instant.parse(instante)));
    }

    private void sesion(Usuario u, String inicio, String ultimoPing) {
        SesionUso s = new SesionUso(u.getId(), Instant.parse(inicio));
        s.registrarPing(Instant.parse(ultimoPing));
        sesiones.save(s);
    }

    private String url(String desde, String hasta) {
        return URL + "?desde=" + desde + "&hasta=" + hasta;
    }

    @Test
    void countsDistinctActiveUsersPerProfileRoleIncludingZeros() throws Exception {
        Usuario e1 = conRol(RolPerfil.ESTUDIANTE);
        Usuario e2 = conRol(RolPerfil.ESTUDIANTE);
        Usuario p1 = conRol(RolPerfil.PROFESIONAL);
        conRol(RolPerfil.VISITANTE); // inactivo
        visita(e1, "2026-03-05T10:00:00Z");
        visita(e1, "2026-03-06T10:00:00Z"); // mismo usuario, cuenta una vez
        sesion(e2, "2026-03-07T10:00:00Z", "2026-03-07T10:20:00Z");
        visita(p1, "2026-03-31T23:59:59Z");

        mvc.perform(getReq(url("2026-03-01", "2026-03-31"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.porRol.length()").value(4))
                .andExpect(jsonPath("$.porRol[?(@.rol=='ESTUDIANTE')].usuarios").value(2))
                .andExpect(jsonPath("$.porRol[?(@.rol=='PROFESIONAL')].usuarios").value(1))
                .andExpect(jsonPath("$.porRol[?(@.rol=='PRACTICANTE')].usuarios").value(0))
                .andExpect(jsonPath("$.porRol[?(@.rol=='VISITANTE')].usuarios").value(0));
    }

    @Test
    void activityOutsideThePeriodDoesNotCount() throws Exception {
        Usuario e1 = conRol(RolPerfil.ESTUDIANTE);
        visita(e1, "2026-02-28T23:59:59Z");
        sesion(e1, "2026-04-01T00:00:00Z", "2026-04-01T00:30:00Z");

        mvc.perform(getReq(url("2026-03-01", "2026-03-31"), admin)).andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void aSessionOverlappingThePeriodCounts() throws Exception {
        Usuario e1 = conRol(RolPerfil.PRACTICANTE);
        sesion(e1, "2026-02-28T23:50:00Z", "2026-03-01T00:10:00Z");

        mvc.perform(getReq(url("2026-03-01", "2026-03-01"), admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.porRol[?(@.rol=='PRACTICANTE')].usuarios").value(1));
    }

    @Test
    void deactivatedAccountsAreNotCounted() throws Exception {
        Usuario e1 = conRol(RolPerfil.ESTUDIANTE);
        visita(e1, "2026-03-05T10:00:00Z");
        e1.setActivo(false);
        usuarios.saveAndFlush(e1);

        mvc.perform(getReq(url("2026-03-01", "2026-03-31"), admin)).andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void defaultsToTheLastThirtyDays() throws Exception {
        mvc.perform(getReq(URL, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").isString())
                .andExpect(jsonPath("$.hasta").isString())
                .andExpect(jsonPath("$.porRol.length()").value(4));
    }

    @Test
    void validatesTheRange() throws Exception {
        mvc.perform(getReq(URL + "?desde=2026-03-01", admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANGO_INVALIDO"));
        mvc.perform(getReq(url("2026-03-31", "2026-03-01"), admin)).andExpect(status().isBadRequest());
        mvc.perform(getReq(url("2024-01-01", "2026-01-01"), admin)).andExpect(status().isBadRequest());
    }

    @Test
    void onlyAdminsCanQuery() throws Exception {
        mvc.perform(getReq(URL, usuario())).andExpect(status().isForbidden());
    }
}
