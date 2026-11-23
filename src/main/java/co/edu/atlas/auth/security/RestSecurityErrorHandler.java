package co.edu.atlas.auth.security;

import co.edu.atlas.common.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Responde 401/403 desde la cadena de filtros con el mismo formato de error de la API. */
@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper mapper;

    public RestSecurityErrorHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        escribir(req, res, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "Debes iniciar sesion");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException {
        escribir(req, res, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "No tienes permiso para esta accion");
    }

    public void escribir(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String code, String msg)
            throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        ApiError body = new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), code, msg, req.getRequestURI(), List.of());
        mapper.writeValue(res.getWriter(), body);
    }
}
