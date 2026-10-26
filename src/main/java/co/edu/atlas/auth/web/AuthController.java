package co.edu.atlas.auth.web;

import co.edu.atlas.auth.dto.LoginRequest;
import co.edu.atlas.auth.dto.RefreshRequest;
import co.edu.atlas.auth.dto.RegistroRequest;
import co.edu.atlas.auth.dto.TokenResponse;
import co.edu.atlas.auth.dto.UsuarioResponse;
import co.edu.atlas.auth.dto.RecuperarRequest;
import co.edu.atlas.auth.dto.RestablecerRequest;
import co.edu.atlas.auth.service.AuthService;
import co.edu.atlas.auth.service.PasswordResetService;
import java.util.Map;
import co.edu.atlas.auth.service.RegistroService;
import jakarta.validation.Valid;
import co.edu.atlas.auth.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final PasswordResetService reset;

    public AuthController(RegistroService registro, AuthService auth, PasswordResetService reset) {
        this.reset = reset;
        this.registro = registro;
        this.auth = auth;
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal AuthenticatedUser actual) {
        return auth.perfil(actual.id());
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> recuperar(@Valid @RequestBody RecuperarRequest request) {
        reset.solicitar(request.correo());
        return Map.of("mensaje", "Si el correo esta registrado, recibiras un enlace para restablecer tu contrasena");
    }

    @PostMapping("/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restablecer(@Valid @RequestBody RestablecerRequest request) {
        reset.restablecer(request.token(), request.nuevaPassword());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return auth.refrescar(request.refreshToken());
    }

    @PostMapping("/cierre-pendiente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cierrePendiente(@Valid @RequestBody RefreshRequest request) {
        auth.programarCierre(request.refreshToken());
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
