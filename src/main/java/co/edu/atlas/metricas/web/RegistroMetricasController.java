package co.edu.atlas.metricas.web;

import co.edu.atlas.auth.security.AuthenticatedUser;
import co.edu.atlas.metricas.dto.SesionResponse;
import co.edu.atlas.metricas.dto.VisitaRequest;
import co.edu.atlas.metricas.service.RegistroMetricasService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Registro de actividad del propio usuario autenticado. No devuelve ni lee datos de otros usuarios. */
@RestController
@RequestMapping("/metricas")
public class RegistroMetricasController {

    private final RegistroMetricasService service;

    public RegistroMetricasController(RegistroMetricasService service) {
        this.service = service;
    }

    @PostMapping("/visitas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void visita(@Valid @RequestBody VisitaRequest request, @AuthenticationPrincipal AuthenticatedUser actual) {
        service.registrarVisita(actual.id(), request.tipo(), request.ejercicioId());
    }

    @PostMapping("/sesion/ping")
    public SesionResponse ping(@AuthenticationPrincipal AuthenticatedUser actual) {
        return service.ping(actual.id());
    }
}
