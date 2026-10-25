package co.edu.atlas.metricas.service;

import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.dto.CategoriaRef;
import co.edu.atlas.ejercicio.dto.RegionRef;
import co.edu.atlas.metricas.Agrupacion;
import co.edu.atlas.metricas.SesionUsoRepository;
import co.edu.atlas.metricas.VisitaRepository;
import co.edu.atlas.metricas.dto.ResumenMetricasResponse;
import co.edu.atlas.metricas.dto.TopEjerciciosResponse;
import co.edu.atlas.metricas.dto.VisitasPeriodoResponse;
import co.edu.atlas.metricas.dto.VisitasPeriodoResponse.Punto;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas agregadas de metricas (HU-30 a HU-34). Solo se expone bajo /admin/metricas. */
@Service
@Transactional(readOnly = true)
public class ConsultaMetricasService {

    static final int DIAS_RESUMEN = 30;

    private final VisitaRepository visitas;
    private final SesionUsoRepository sesiones;
    private final Clock clock;
    private final EjercicioRepository ejercicios;

    public ConsultaMetricasService(
            VisitaRepository visitas, SesionUsoRepository sesiones, Clock clock, EjercicioRepository ejercicios) {
        this.ejercicios = ejercicios;
        this.visitas = visitas;
        this.sesiones = sesiones;
        this.clock = clock;
    }

    public ResumenMetricasResponse resumen() {
        LocalDate hoy = clock.instant().atOffset(ZoneOffset.UTC).toLocalDate();
        return new ResumenMetricasResponse(
                visitas.count(), visitas.countByFechaGreaterThanEqual(hoy.minusDays(DIAS_RESUMEN - 1L)), sesiones.count());
    }

    /**
     * Visitas del periodo (HU-31). Se agrega por dia en la base (una sola consulta indexada, SQL estandar) y se
     * agrupa por semana (lunes) o mes en memoria: como mucho 366 filas. Los cubos vacios se devuelven con cero.
     */
    public VisitasPeriodoResponse visitasPorPeriodo(LocalDate desde, LocalDate hasta, Agrupacion agrupacion) {
        Periodo periodo = Periodo.validar(desde, hasta);
        Agrupacion grupo = agrupacion == null ? Agrupacion.DIA : agrupacion;
        Map<LocalDate, Long> porDia = new HashMap<>();
        for (Object[] fila : visitas.contarPorDia(periodo.desde(), periodo.hasta())) {
            porDia.put((LocalDate) fila[0], (Long) fila[1]);
        }
        Map<LocalDate, Long> cubos = new TreeMap<>();
        long total = 0;
        for (LocalDate dia = periodo.desde(); !dia.isAfter(periodo.hasta()); dia = dia.plusDays(1)) {
            long n = porDia.getOrDefault(dia, 0L);
            total += n;
            cubos.merge(inicioDeCubo(dia, grupo), n, Long::sum);
        }
        List<Punto> serie = new ArrayList<>();
        cubos.forEach((inicio, n) -> serie.add(new Punto(inicio, n)));
        return new VisitasPeriodoResponse(periodo.desde(), periodo.hasta(), grupo, total, serie);
    }

    /**
     * Fichas publicadas mas consultadas (HU-32), con {@code limite} entradas como maximo. Sin rango se usa todo el
     * historico. Solo aparecen fichas con visualizaciones registradas.
     */
    public TopEjerciciosResponse masConsultados(LocalDate desde, LocalDate hasta, int limite) {
        if ((desde == null) != (hasta == null)) {
            throw ApiException.badRequest("RANGO_INVALIDO", "Indica desde y hasta juntos, o ninguno");
        }
        boolean conRango = desde != null;
        if (conRango) {
            Periodo.validar(desde, hasta);
        }
        List<Object[]> filas = visitas.masConsultados(
                conRango ? desde : LocalDate.of(2000, 1, 1),
                conRango ? hasta : LocalDate.of(2999, 12, 31),
                PageRequest.of(0, limite));
        Map<Long, Ejercicio> fichas = new HashMap<>();
        if (!filas.isEmpty()) {
            ejercicios.findConRegionYCategoria(filas.stream().map(f -> (Long) f[0]).toList())
                    .forEach(e -> fichas.put(e.getId(), e));
        }
        List<TopEjerciciosResponse.Item> items = new ArrayList<>();
        for (Object[] fila : filas) {
            Ejercicio e = fichas.get((Long) fila[0]);
            items.add(new TopEjerciciosResponse.Item(
                    e.getId(),
                    e.getNombre(),
                    new RegionRef(e.getRegion().getId(), e.getRegion().getNombre(), e.getRegion().getVista(), e.getRegion().getSvgId()),
                    new CategoriaRef(e.getCategoria().getId(), e.getCategoria().getNombre()),
                    (Long) fila[1]));
        }
        return new TopEjerciciosResponse(desde, hasta, limite, items);
    }

    private static LocalDate inicioDeCubo(LocalDate dia, Agrupacion grupo) {
        return switch (grupo) {
            case DIA -> dia;
            case SEMANA -> dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES -> dia.withDayOfMonth(1);
        };
    }
}
