package co.edu.atlas.auth.security;

import co.edu.atlas.auth.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee el access token del encabezado Authorization y autentica la peticion. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIX)) {
            autenticar(header.substring(PREFIX.length()).trim());
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token) {
        try {
            AuthenticatedUser user = jwt.parsear(token);
            var authority = new SimpleGrantedAuthority("ROLE_" + user.rol().name());
            var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }
    }
}
