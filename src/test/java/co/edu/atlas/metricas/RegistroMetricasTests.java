package co.edu.atlas.metricas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.support.TestClockConfig;
import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.support.ApiIntegrationTest;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(TestClockConfig.class)
class RegistroMetricasTests extends ApiIntegrationTest {

    @Autowired
    VisitaRepository visitas;

    @Autowired
    SesionUsoRepository sesiones;

    @Autowired
    MutableClock clock;

    Usuario carla;
    long ficha;

    @BeforeEach
    void preparar() throws Exception {
        carla = usuario();
        ficha = crearPublicada(admin(), "Ficha");
    }

    private void visita(Usuario u, Map<String, Object> cuerpo, int estado) throws Exception {
        mvc.perform(jsonDe(post("/metricas/visitas"), u, cuerpo)).andExpect(status().is(estado));
    }

    @Test
    void recordsAFichaVisitWithDateAndUser() throws Exception {
        visita(carla, Map.of("tipo", "FICHA", "ejercicioId", ficha), 204);

        Visita v = visitas.findAll().get(0);
        assertThat(v.getUsuarioId()).isEqualTo(carla.getId());
        assertThat(v.getTipo()).isEqualTo(TipoVisita.FICHA);
        assertThat(v.getEjercicioId()).isEqualTo(ficha);
        assertThat(v.getFecha()).isEqualTo(clock.instant().atZone(java.time.ZoneOffset.UTC).toLocalDate());
    }

    @Test
    void recordsAnAtlasVisit() throws Exception {
        visita(carla, Map.of("tipo", "ATLAS"), 204);

        assertThat(visitas.findAll()).extracting(Visita::getTipo).containsExactly(TipoVisita.ATLAS);
    }

    @Test
    void ignoresImmediateRepeatsButCountsLaterOnes() throws Exception {
        long antes = visitas.count();
        visita(carla, Map.of("tipo", "FICHA", "ejercicioId", ficha), 204);
        visita(carla, Map.of("tipo", "FICHA", "ejercicioId", ficha), 204);
        assertThat(visitas.count()).isEqualTo(antes + 1);

        clock.advance(Duration.ofSeconds(11));
        visita(carla, Map.of("tipo", "FICHA", "ejercicioId", ficha), 204);
        assertThat(visitas.count()).isEqualTo(antes + 2);
    }

    @Test
    void differentUsersAreNotDeduplicatedAgainstEachOther() throws Exception {
        long antes = visitas.count();
        visita(carla, Map.of("tipo", "ATLAS"), 204);
        visita(usuario(), Map.of("tipo", "ATLAS"), 204);

        assertThat(visitas.count()).isEqualTo(antes + 2);
    }

    @Test
    void validatesTheDestination() throws Exception {
        visita(carla, Map.of("tipo", "FICHA"), 400);
        visita(carla, Map.of("tipo", "ATLAS", "ejercicioId", ficha), 400);
        mvc.perform(jsonDe(post("/metricas/visitas"), carla, Map.of("ejercicioId", ficha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(jsonDe(post("/metricas/visitas"), carla, Map.of("tipo", "OTRO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void draftOrMissingSheetIsNotFound() throws Exception {
        long borrador = crearBorrador(admin(), fichaMinima("Borrador"));

        visita(carla, Map.of("tipo", "FICHA", "ejercicioId", borrador), 404);
        visita(carla, Map.of("tipo", "FICHA", "ejercicioId", 999999), 404);
    }

    @Test
    void firstPingOpensASession() throws Exception {
        mvc.perform(postReq("/metricas/sesion/ping", carla))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nuevaSesion").value(true))
                .andExpect(jsonPath("$.duracionSegundos").value(0))
                .andExpect(jsonPath("$.sesionId").isNumber());
    }

    @Test
    void pingsWithinThirtyMinutesExtendTheSameSession() throws Exception {
        long id = cuerpo(mvc.perform(postReq("/metricas/sesion/ping", carla)).andReturn()).get("sesionId").asLong();

        clock.advance(Duration.ofMinutes(10));
        mvc.perform(postReq("/metricas/sesion/ping", carla))
                .andExpect(jsonPath("$.nuevaSesion").value(false))
                .andExpect(jsonPath("$.sesionId").value(id))
                .andExpect(jsonPath("$.duracionSegundos").value(600));
        clock.advance(Duration.ofMinutes(30));
        mvc.perform(postReq("/metricas/sesion/ping", carla))
                .andExpect(jsonPath("$.sesionId").value(id))
                .andExpect(jsonPath("$.duracionSegundos").value(2400));
    }

    @Test
    void pingAfterMoreThanThirtyMinutesOfInactivityStartsANewSession() throws Exception {
        long id = cuerpo(mvc.perform(postReq("/metricas/sesion/ping", carla)).andReturn()).get("sesionId").asLong();

        clock.advance(Duration.ofMinutes(30).plusSeconds(1));
        mvc.perform(postReq("/metricas/sesion/ping", carla))
                .andExpect(jsonPath("$.nuevaSesion").value(true))
                .andExpect(jsonPath("$.sesionId").value(org.hamcrest.Matchers.not((int) id)))
                .andExpect(jsonPath("$.duracionSegundos").value(0));
    }

    @Test
    void veryFrequentPingsDoNotRewriteTheSession() throws Exception {
        mvc.perform(postReq("/metricas/sesion/ping", carla)).andExpect(status().isOk());
        clock.advance(Duration.ofSeconds(2));

        mvc.perform(postReq("/metricas/sesion/ping", carla))
                .andExpect(jsonPath("$.nuevaSesion").value(false))
                .andExpect(jsonPath("$.duracionSegundos").value(0));
    }

    @Test
    void sessionsAreKeptPerUser() throws Exception {
        mvc.perform(postReq("/metricas/sesion/ping", carla)).andExpect(jsonPath("$.nuevaSesion").value(true));

        mvc.perform(postReq("/metricas/sesion/ping", usuario())).andExpect(jsonPath("$.nuevaSesion").value(true));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(post("/metricas/sesion/ping")).andExpect(status().isUnauthorized());
        mvc.perform(post("/metricas/visitas").contentType("application/json").content("{\"tipo\":\"ATLAS\"}"))
                .andExpect(status().isUnauthorized());
    }
}
