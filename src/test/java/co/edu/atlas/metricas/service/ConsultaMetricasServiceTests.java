package co.edu.atlas.metricas.service;

import static co.edu.atlas.support.Entidades.conId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.usuario.RolPerfil;
import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.metricas.Agrupacion;
import co.edu.atlas.metricas.SesionUsoRepository;
import co.edu.atlas.metricas.UsuariosActivosRepository;
import co.edu.atlas.metricas.VisitaRepository;
import co.edu.atlas.metricas.dto.ResumenMetricasResponse;
import co.edu.atlas.metricas.dto.TiempoPromedioResponse;
import co.edu.atlas.metricas.dto.TopEjerciciosResponse;
import co.edu.atlas.metricas.dto.UsuariosActivosResponse;
import co.edu.atlas.metricas.dto.VisitasPeriodoResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ConsultaMetricasServiceTests {

    @Mock VisitaRepository visitas;
    @Mock SesionUsoRepository sesiones;
    @Mock EjercicioRepository ejercicios;
    @Mock UsuariosActivosRepository activos;

    /** 2026-01-01 (UTC): los ultimos 30 dias van del 2025-12-03 al 2026-01-01. */
    MutableClock clock = new MutableClock();

    private ConsultaMetricasService servicio() {
        return new ConsultaMetricasService(visitas, sesiones, clock, ejercicios, activos);
    }

    @Test
    void resumenCuentaVisitasDelUltimoMesYSesiones() {
        when(visitas.count()).thenReturn(10L);
        when(visitas.countByFechaGreaterThanEqual(LocalDate.of(2025, 12, 3))).thenReturn(4L);
        when(sesiones.count()).thenReturn(3L);

        ResumenMetricasResponse r = servicio().resumen();

        assertThat(r).isEqualTo(new ResumenMetricasResponse(10, 4, 3));
    }

    @Test
    void visitasPorSemanaAgrupanYRellenanLaSerie() {
        LocalDate desde = LocalDate.of(2026, 1, 5);
        LocalDate hasta = LocalDate.of(2026, 1, 11);
        when(visitas.contarPorDia(desde, hasta)).thenReturn(List.<Object[]>of(new Object[] {LocalDate.of(2026, 1, 6), 3L}));

        VisitasPeriodoResponse r = servicio().visitasPorPeriodo(desde, hasta, Agrupacion.SEMANA);

        assertThat(r.total()).isEqualTo(3);
        assertThat(r.serie()).containsExactly(new VisitasPeriodoResponse.Punto(desde, 3));
    }

    @Test
    void visitasConRangoInvertidoFallan() {
        assertThatThrownBy(() -> servicio().visitasPorPeriodo(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 1), null))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("RANGO_INVALIDO"));
    }

    @Test
    void masConsultadosIncluyeLasVisitasDeLaFicha() {
        Ejercicio ficha = conId(new Ejercicio("Puente", mock(RegionAnatomica.class), mock(CategoriaTerapeutica.class), 1L), 5L);
        when(visitas.masConsultados(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[] {5L, 7L}));
        when(ejercicios.findConRegionYCategoria(anyList())).thenReturn(List.of(ficha));

        TopEjerciciosResponse r = servicio().masConsultados(null, null, 10);

        assertThat(r.items()).hasSize(1);
        assertThat(r.items().get(0).ejercicioId()).isEqualTo(5L);
        assertThat(r.items().get(0).visitas()).isEqualTo(7L);
    }

    @Test
    void masConsultadosConSoloUnExtremoFalla() {
        assertThatThrownBy(() -> servicio().masConsultados(LocalDate.of(2026, 1, 1), null, 10))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("RANGO_INVALIDO"));
    }

    @Test
    void masConsultadosSinVisitasNoBuscaFichas() {
        when(visitas.masConsultados(any(), any(), any(Pageable.class))).thenReturn(List.of());

        assertThat(servicio().masConsultados(null, null, 10).items()).isEmpty();
        verify(ejercicios, never()).findConRegionYCategoria(anyList());
    }

    @Test
    void usuariosActivosCuentaPorRolIncluyendoLosRolesSinActividad() {
        when(activos.contarActivosPorRol(any(), any(), any(), any())).thenReturn(List.<Object[]>of(
                new Object[] {RolPerfil.ESTUDIANTE, 2L}, new Object[] {RolPerfil.VISITANTE, 1L}));

        UsuariosActivosResponse r = servicio().usuariosActivosPorRol(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));

        assertThat(r.total()).isEqualTo(3);
        assertThat(r.porRol()).extracting(UsuariosActivosResponse.PorRol::usuarios).containsExactly(2L, 0L, 0L, 1L);
    }

    @Test
    void usuariosActivosSinRangoUsaLosUltimos30Dias() {
        when(activos.contarActivosPorRol(any(), any(), any(), any())).thenReturn(List.of());

        UsuariosActivosResponse r = servicio().usuariosActivosPorRol(null, null);

        assertThat(r.desde()).isEqualTo(LocalDate.of(2025, 12, 3));
        assertThat(r.hasta()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void tiempoPromedioCompara7DiasConLosAnterioresYCalculaLaVariacion() {
        when(sesiones.promedioDuracion(any(Instant.class), any(Instant.class))).thenReturn(
                List.<Object[]>of(new Object[] {120.0, 4L}), List.<Object[]>of(new Object[] {100.0, 2L}));

        TiempoPromedioResponse r = servicio().tiempoPromedio(LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 14));

        assertThat(r.promedioSegundos()).isEqualTo(120L);
        assertThat(r.anterior().promedioSegundos()).isEqualTo(100L);
        assertThat(r.variacionPorcentaje()).isEqualTo(20.0);
    }

    @Test
    void tiempoPromedioSinSesionesNoTieneVariacion() {
        when(sesiones.promedioDuracion(any(Instant.class), any(Instant.class)))
                .thenReturn(List.<Object[]>of(new Object[] {null, 0L}));

        TiempoPromedioResponse r = servicio().tiempoPromedio(LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 14));

        assertThat(r.promedioSegundos()).isNull();
        assertThat(r.variacionPorcentaje()).isNull();
    }
}
