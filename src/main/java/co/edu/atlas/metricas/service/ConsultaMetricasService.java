package co.edu.atlas.metricas.service;

import co.edu.atlas.metricas.SesionUsoRepository;
import co.edu.atlas.metricas.VisitaRepository;
import co.edu.atlas.metricas.dto.ResumenMetricasResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
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

    public ConsultaMetricasService(VisitaRepository visitas, SesionUsoRepository sesiones, Clock clock) {
        this.visitas = visitas;
        this.sesiones = sesiones;
        this.clock = clock;
    }

    public ResumenMetricasResponse resumen() {
        LocalDate hoy = clock.instant().atOffset(ZoneOffset.UTC).toLocalDate();
        return new ResumenMetricasResponse(
                visitas.count(), visitas.countByFechaGreaterThanEqual(hoy.minusDays(DIAS_RESUMEN - 1L)), sesiones.count());
    }
}
