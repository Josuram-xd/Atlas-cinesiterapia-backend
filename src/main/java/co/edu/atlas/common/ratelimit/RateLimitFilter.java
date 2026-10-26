package co.edu.atlas.common.ratelimit;

import co.edu.atlas.auth.security.RestSecurityErrorHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/** Limita los POST a las rutas sensibles de autenticacion por IP de cliente. */
public class RateLimitFilter extends OncePerRequestFilter {

    static final Set<String> RUTAS_LIMITADAS = Set.of("/auth/login", "/auth/registro", "/auth/recuperar", "/auth/restablecer");

    private final RateLimiter limiter;
    private final RestSecurityErrorHandler errores;

    public RateLimitFilter(RateLimiter limiter, RestSecurityErrorHandler errores) {
        this.limiter = limiter;
        this.errores = errores;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod()) || !RUTAS_LIMITADAS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String clave = request.getRemoteAddr() + "|" + request.getRequestURI();
        if (limiter.tryAcquire(clave)) {
            chain.doFilter(request, response);
            return;
        }
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(limiter.retryAfterSeconds()));
        errores.escribir(
                request, response, HttpStatus.TOO_MANY_REQUESTS, "DEMASIADOS_INTENTOS",
                "Demasiados intentos, espera un momento e intenta de nuevo");
    }
}
