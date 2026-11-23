package co.edu.atlas.metricas.service;

import static co.edu.atlas.support.Entidades.conId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.metricas.MetricasProperties;
import co.edu.atlas.metricas.SesionUso;
import co.edu.atlas.metricas.SesionUsoRepository;
import co.edu.atlas.metricas.TipoVisita;
import co.edu.atlas.metricas.Visita;
import co.edu.atlas.metricas.VisitaRepository;
import co.edu.atlas.metricas.dto.SesionResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistroMetricasServiceTests {

    @Mock VisitaRepository visitas;
    @Mock SesionUsoRepository sesiones;
    @Mock EjercicioRepository ejercicios;

    MutableClock clock = new MutableClock();
    MetricasProperties props = new MetricasProperties(
            Duration.ofMinutes(30), Duration.ofSeconds(10), Duration.ofSeconds(5), 30);

    private RegistroMetricasService servicio() {
        return new RegistroMetricasService(visitas, sesiones, ejercicios, props, clock);
    }

    private static Ejercicio ficha(boolean publicada) {
        Ejercicio e = conId(new Ejercicio("Puente", null, null, 1L), 5L);
        if (publicada) {
            e.publicar(Instant.now());
        }
        return e;
    }

    @Test
    void visitaAlAtlasSeGuarda() {
        servicio().registrarVisita(1L, TipoVisita.ATLAS, null);

        verify(visitas).save(any(Visita.class));
    }

    @Test
    void visitaAlAtlasNoAdmiteUnaFicha() {
        assertThatThrownBy(() -> servicio().registrarVisita(1L, TipoVisita.ATLAS, 5L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("VISITA_INVALIDA"));
        verify(visitas, never()).save(any());
    }

    @Test
    void visitaAUnaFichaRequiereSuId() {
        assertThatThrownBy(() -> servicio().registrarVisita(1L, TipoVisita.FICHA, null))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("VISITA_INVALIDA"));
    }

    @Test
    void visitaAUnaFichaNoPublicadaFallaComoNoEncontrada() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(false)));

        assertThatThrownBy(() -> servicio().registrarVisita(1L, TipoVisita.FICHA, 5L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
        verify(visitas, never()).save(any());
    }

    @Test
    void visitaAUnaFichaPublicadaSeGuarda() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(true)));

        servicio().registrarVisita(1L, TipoVisita.FICHA, 5L);

        verify(visitas).save(any(Visita.class));
    }

    @Test
    void unaRepeticionInmediataSeIgnoraSinError() {
        when(visitas.existsByUsuarioIdAndTipoAndEjercicioIdAndCreatedAtAfter(any(), any(), any(), any())).thenReturn(true);

        servicio().registrarVisita(1L, TipoVisita.ATLAS, null);

        verify(visitas, never()).save(any());
    }

    @Test
    void masVisitasDeLasPermitidasPorMinutoFallaCon429() {
        RegistroMetricasService servicio = servicio();
        for (int i = 0; i < props.maxVisitasPorMinuto(); i++) {
            servicio.registrarVisita(1L, TipoVisita.ATLAS, null);
        }

        assertThatThrownBy(() -> servicio.registrarVisita(1L, TipoVisita.ATLAS, null))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("DEMASIADOS_INTENTOS"));
    }

    @Test
    void primerPingAbreUnaSesionNueva() {
        when(sesiones.findFirstByUsuarioIdOrderByUltimoPingAtDesc(1L)).thenReturn(Optional.empty());
        when(sesiones.save(any(SesionUso.class))).thenAnswer(i -> i.getArgument(0));

        SesionResponse r = servicio().ping(1L);

        assertThat(r.nuevaSesion()).isTrue();
    }

    @Test
    void pingDentroDeLaSesionVigenteLaContinua() {
        SesionUso sesion = new SesionUso(1L, clock.instant());
        when(sesiones.findFirstByUsuarioIdOrderByUltimoPingAtDesc(1L)).thenReturn(Optional.of(sesion));
        clock.advance(Duration.ofSeconds(10));

        SesionResponse r = servicio().ping(1L);

        assertThat(r.nuevaSesion()).isFalse();
        assertThat(sesion.getUltimoPingAt()).isEqualTo(clock.instant());
    }

    @Test
    void pingDemasiadoSeguidoNoEscribeOtroPing() {
        SesionUso sesion = new SesionUso(1L, clock.instant());
        Instant inicial = sesion.getUltimoPingAt();
        when(sesiones.findFirstByUsuarioIdOrderByUltimoPingAtDesc(1L)).thenReturn(Optional.of(sesion));
        clock.advance(Duration.ofSeconds(1));

        servicio().ping(1L);

        assertThat(sesion.getUltimoPingAt()).isEqualTo(inicial);
    }

    @Test
    void pingTrasInactividadAbreOtraSesion() {
        when(sesiones.findFirstByUsuarioIdOrderByUltimoPingAtDesc(1L))
                .thenReturn(Optional.of(new SesionUso(1L, clock.instant())));
        when(sesiones.save(any(SesionUso.class))).thenAnswer(i -> i.getArgument(0));
        clock.advance(Duration.ofMinutes(31));

        assertThat(servicio().ping(1L).nuevaSesion()).isTrue();
    }
}
