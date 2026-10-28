package co.edu.atlas.comentario.web;

import co.edu.atlas.comentario.dto.PalabraRequest;
import co.edu.atlas.comentario.dto.PalabraResponse;
import co.edu.atlas.comentario.service.PalabraOfensivaService;
import co.edu.atlas.common.web.Pagina;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/admin/palabras-ofensivas")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class PalabraOfensivaAdminController {

    private final PalabraOfensivaService service;

    public PalabraOfensivaAdminController(PalabraOfensivaService service) {
        this.service = service;
    }

    @GetMapping
    public Pagina<PalabraResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listar(q, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PalabraResponse crear(@Valid @RequestBody PalabraRequest request) {
        return service.crear(request.palabra());
    }

    @PutMapping("/{id}")
    public PalabraResponse actualizar(@PathVariable Long id, @Valid @RequestBody PalabraRequest request) {
        return service.actualizar(id, request.palabra());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
