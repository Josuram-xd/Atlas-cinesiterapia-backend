package co.edu.atlas.metricas.web;

import co.edu.atlas.metricas.dto.ResumenMetricasResponse;
import co.edu.atlas.metricas.service.ConsultaMetricasService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
