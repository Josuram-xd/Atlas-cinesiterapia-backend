package co.edu.atlas.auth.web;

import co.edu.atlas.auth.dto.LoginRequest;
import co.edu.atlas.auth.dto.RefreshRequest;
import co.edu.atlas.auth.dto.RegistroRequest;
import co.edu.atlas.auth.dto.TokenResponse;
import co.edu.atlas.auth.dto.UsuarioResponse;
import co.edu.atlas.auth.service.AuthService;
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

    private final AuthService auth;

    public AuthController(RegistroService registro, AuthService auth) {
        this.registro = registro;
        this.auth = auth;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return auth.refrescar(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        auth.logout(request.refreshToken());
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
        return UsuarioResponse.de(registro.registrar(request));
    }
}
