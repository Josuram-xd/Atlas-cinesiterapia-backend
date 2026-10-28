package co.edu.atlas.auth.jwt;

import co.edu.atlas.auth.security.AuthenticatedUser;
import co.edu.atlas.auth.usuario.RolSistema;
import co.edu.atlas.auth.usuario.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final JwtProperties props;
    private final Clock clock;

    public JwtService(JwtProperties props, Clock clock) {
        byte[] secret = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + MIN_SECRET_BYTES + " bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.props = props;
        this.clock = clock;
    }

    public String generarAccessToken(Usuario usuario) {
        Date emitido = Date.from(clock.instant());
        Date expira = Date.from(clock.instant().plus(props.accessTtl()));
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRolSistema().name())
                .issuedAt(emitido)
                .expiration(expira)
                .signWith(key)
                .compact();
    }

    /** Valida firma y expiracion; lanza {@link JwtException} si el token no es valido. */
    public AuthenticatedUser parsear(String token) {
        var claims = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthenticatedUser(
                Long.valueOf(claims.getSubject()),
                claims.get("correo", String.class),
                RolSistema.valueOf(claims.get("rol", String.class)));
    }

    public long accessTtlSegundos() {
        return props.accessTtl().toSeconds();
    }
}
