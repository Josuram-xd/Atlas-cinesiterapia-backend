package co.edu.atlas.media.web;

import co.edu.atlas.media.dto.MediaAdminResponse;
import co.edu.atlas.media.dto.SolicitudSubida;
import co.edu.atlas.media.dto.SubidaResponse;
import co.edu.atlas.media.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ejercicios/{ejercicioId}/media")
@PreAuthorize("hasRole('ADMIN')")
public class MediaAdminController {

    private final MediaService service;

    public MediaAdminController(MediaService service) {
        this.service = service;
    }

    @PostMapping("/subida")
    @ResponseStatus(HttpStatus.CREATED)
    public SubidaResponse solicitarSubida(@PathVariable Long ejercicioId, @Valid @RequestBody SolicitudSubida request) {
        return service.solicitarSubida(ejercicioId, request);
    }

    @PostMapping("/{mediaId}/confirmar")
    public MediaAdminResponse confirmar(@PathVariable Long ejercicioId, @PathVariable Long mediaId) {
        return service.confirmar(ejercicioId, mediaId);
    }

    @DeleteMapping("/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long ejercicioId, @PathVariable Long mediaId) {
        service.eliminar(ejercicioId, mediaId);
    }
}
