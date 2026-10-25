package co.edu.atlas.comentario.web;

import co.edu.atlas.comentario.TipoRevision;
import co.edu.atlas.comentario.dto.ComentarioRevisionResponse;
import co.edu.atlas.comentario.service.ModeracionComentarioService;
import co.edu.atlas.common.web.Pagina;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Moderacion de comentarios (HU-26 a HU-28). Solo administradores. */
@RestController
@RequestMapping("/admin/comentarios")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class ComentarioAdminController {

    private final ModeracionComentarioService service;

    public ComentarioAdminController(ModeracionComentarioService service) {
        this.service = service;
    }

    @GetMapping("/revision")
    public Pagina<ComentarioRevisionResponse> revision(
            @RequestParam(required = false) TipoRevision tipo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.cola(tipo, page, size);
    }
}
