package co.edu.atlas.common.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Solo desarrollo: escribe el correo en el log (incluye enlaces con tokens, por eso no se usa en prod). */
@Component
@Profile("!prod")
public class LogCorreoSender implements CorreoSender {

    private static final Logger log = LoggerFactory.getLogger(LogCorreoSender.class);

    @Override
    public void enviar(String destino, String asunto, String cuerpo) {
        log.info("[CORREO simulado] para={} asunto={}\n{}", destino, asunto, cuerpo);
    }
}
