package co.edu.atlas.auth.web;

import co.edu.atlas.auth.dto.RegistroRequest;
import co.edu.atlas.auth.dto.UsuarioResponse;
import co.edu.atlas.auth.service.RegistroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistroService registro;

    public AuthController(RegistroService registro) {
        this.registro = registro;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
        return UsuarioResponse.de(registro.registrar(request));
    }
}
