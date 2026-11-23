package co.edu.atlas.common.mail;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Produccion con BREVO_API_KEY: envia por la API transaccional de Brevo. Un error HTTP lanza excepcion. */
@Component
@Profile("prod")
@ConditionalOnExpression("!'${BREVO_API_KEY:}'.isBlank()")
public class BrevoCorreoSender implements CorreoSender {

    static final String URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient http;
    private final String remitente;
    private final String nombreRemitente;

    public BrevoCorreoSender(
            RestClient.Builder builder,
            @Value("${BREVO_API_KEY}") String apiKey,
            @Value("${BREVO_SENDER_EMAIL}") String remitente,
            @Value("${BREVO_SENDER_NAME:Atlas de Cinesiterapia}") String nombreRemitente) {
        this.http = builder.defaultHeader("api-key", apiKey).build();
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
    }

    @Override
    public void enviar(String destino, String asunto, String cuerpo) {
        http.post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "sender", Map.of("name", nombreRemitente, "email", remitente),
                        "to", List.of(Map.of("email", destino)),
                        "subject", asunto,
                        "textContent", cuerpo))
                .retrieve()
                .toBodilessEntity();
    }
}
