package co.edu.atlas.auth.jwt;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/** Configuracion JWT. El secreto viene de JWT_SECRET (minimo 32 bytes). */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @DefaultValue("15m") Duration accessTtl,
        @DefaultValue("7d") Duration refreshTtl) {}
