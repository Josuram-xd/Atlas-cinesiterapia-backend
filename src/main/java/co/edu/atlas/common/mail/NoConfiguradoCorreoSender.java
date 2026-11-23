package co.edu.atlas.common.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Produccion mientras no exista proveedor de correo: no envia nada y nunca registra el contenido. */
@Component
@Profile("prod")
public class NoConfiguradoCorreoSender implements CorreoSender {

    private static final Logger log = LoggerFactory.getLogger(NoConfiguradoCorreoSender.class);

    @Override
    public void enviar(String destino, String asunto, String cuerpo) {
        log.warn("Proveedor de correo no configurado: no se envio el mensaje '{}'", asunto);
    }
}
