package co.edu.atlas.ejercicio.web;

import co.edu.atlas.auth.security.AuthenticatedUser;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResponse;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResumen;
import co.edu.atlas.ejercicio.dto.EjercicioRequest;
import co.edu.atlas.ejercicio.service.EjercicioAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ejercicios")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class EjercicioAdminController {

    private final EjercicioAdminService service;

    public EjercicioAdminController(EjercicioAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioAdminResponse crear(
            @Valid @RequestBody EjercicioRequest request, @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.crear(request, actual.id());
    }

    @GetMapping
    public Pagina<EjercicioAdminResumen> listar(
            @RequestParam(required = false) EstadoEjercicio estado,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listar(estado, regionId, categoriaId, q, page, size);
    }

    @GetMapping("/{id}")
    public EjercicioAdminResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/publicar")
    public EjercicioAdminResponse publicar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.publicar(id, actual.id());
    }

    @PostMapping("/{id}/archivar")
    public EjercicioAdminResponse archivar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.archivar(id, actual.id());
    }

    @PostMapping("/{id}/restaurar")
    public EjercicioAdminResponse restaurar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.restaurar(id, actual.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actual) {
        service.eliminar(id, actual.id());
    }

    @PutMapping("/{id}")
    public EjercicioAdminResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EjercicioRequest request,
            @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.actualizar(id, request, actual.id());
    }
}
