package co.edu.atlas.comentario.web;

import co.edu.atlas.auth.security.AuthenticatedUser;
import co.edu.atlas.comentario.dto.ComentarioRequest;
import co.edu.atlas.comentario.dto.ComentarioResponse;
import co.edu.atlas.comentario.dto.ReporteResponse;
import co.edu.atlas.comentario.service.ComentarioService;
import co.edu.atlas.comentario.service.ReporteComentarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Edicion y eliminacion del comentario propio (HU-22, HU-23) respuestas (HU-29) y reportes (HU-25). */
@RestController
@RequestMapping("/comentarios")
public class ComentarioPropioController {

    private final ComentarioService service;
    private final ReporteComentarioService reportes;

    public ComentarioPropioController(ComentarioService service, ReporteComentarioService reportes) {
        this.service = service;
        this.reportes = reportes;
    }

    @PostMapping("/{id}/reportes")
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse reportar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actual) {
        return reportes.reportar(id, actual.id());
    }

    @PutMapping("/{id}")
    public ComentarioResponse editar(
            @PathVariable Long id, @Valid @RequestBody ComentarioRequest request, @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.editar(id, request.texto(), actual.id());
    }

    @PostMapping("/{id}/respuestas")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse responder(
            @PathVariable Long id, @Valid @RequestBody ComentarioRequest request, @AuthenticationPrincipal AuthenticatedUser actual) {
        return service.responder(id, request.texto(), actual.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actual) {
        service.eliminar(id, actual.id());
    }
}
