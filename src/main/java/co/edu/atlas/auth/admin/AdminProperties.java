package co.edu.atlas.auth.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Credenciales del primer administrador (ADMIN_EMAIL / ADMIN_PASSWORD). Si faltan, no se hace seed. */
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(@DefaultValue("") String email, @DefaultValue("") String password) {

    public boolean configurado() {
        return !email.isBlank() && !password.isBlank();
    }
}
