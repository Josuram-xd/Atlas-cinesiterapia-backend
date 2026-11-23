package co.edu.atlas.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Ajustes del modulo de autenticacion (prefijo app.auth). */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        @DefaultValue("http://localhost:5173") String frontendUrl,
        @DefaultValue("30m") Duration resetTtl,
        @DefaultValue("60s") Duration closeGrace) {}
