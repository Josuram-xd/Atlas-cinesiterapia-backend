package co.edu.atlas.comentario.web;

import co.edu.atlas.auth.security.AuthenticatedUser;
import co.edu.atlas.comentario.dto.ComentarioRequest;
import co.edu.atlas.comentario.dto.ComentarioResponse;
import co.edu.atlas.comentario.service.ComentarioService;
import co.edu.atlas.common.web.Pagina;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ejercicios/{ejercicioId}/comentarios")
@Validated
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping
    public Pagina<ComentarioResponse> listar(
            @PathVariable Long ejercicioId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.listar(ejercicioId, actual.id(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse crear(
            @PathVariable Long ejercicioId,
            @Valid @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.crear(ejercicioId, request.texto(), actual.id());
    }
}
