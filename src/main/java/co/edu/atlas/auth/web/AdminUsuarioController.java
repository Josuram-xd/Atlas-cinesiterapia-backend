package co.edu.atlas.auth.web;

import co.edu.atlas.auth.admin.UsuarioAdminService;
import co.edu.atlas.auth.dto.UsuarioResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioAdminService service;

    public AdminUsuarioController(UsuarioAdminService service) {
        this.service = service;
    }

    @PostMapping("/{id}/promover")
    public UsuarioResponse promover(@PathVariable Long id) {
        return service.promover(id);
    }
}
