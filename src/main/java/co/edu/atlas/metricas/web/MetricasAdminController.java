package co.edu.atlas.metricas.web;

import co.edu.atlas.metricas.Agrupacion;
import co.edu.atlas.metricas.dto.ResumenMetricasResponse;
import co.edu.atlas.metricas.dto.TopEjerciciosResponse;
import co.edu.atlas.metricas.dto.UsuariosActivosResponse;
import co.edu.atlas.metricas.dto.VisitasPeriodoResponse;
import co.edu.atlas.metricas.service.ConsultaMetricasService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Panel de metricas de uso (HU-30): todo el modulo es exclusivo de administradores. */
@RestController
@RequestMapping("/admin/metricas")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class MetricasAdminController {

    private final ConsultaMetricasService service;

    public MetricasAdminController(ConsultaMetricasService service) {
        this.service = service;
    }

    @GetMapping
    public ResumenMetricasResponse resumen() {
        return service.resumen();
    }

    @GetMapping("/visitas")
    public VisitasPeriodoResponse visitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") Agrupacion agrupacion) {
        return service.visitasPorPeriodo(desde, hasta, agrupacion);
    }

    @GetMapping("/ejercicios-mas-consultados")
    public TopEjerciciosResponse masConsultados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite) {
        return service.masConsultados(desde, hasta, limite);
    }

    @GetMapping("/usuarios-activos")
    public UsuariosActivosResponse usuariosActivos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.usuariosActivosPorRol(desde, hasta);
    }
}
