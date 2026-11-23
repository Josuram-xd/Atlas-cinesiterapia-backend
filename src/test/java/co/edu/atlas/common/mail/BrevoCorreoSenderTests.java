package co.edu.atlas.common.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class BrevoCorreoSenderTests {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
    private final BrevoCorreoSender sender = new BrevoCorreoSender(builder, "clave", "app@atlas.test", "Atlas");

    @Test
    void sendsTheEmailThroughBrevoWithTheApiKey() {
        servidor.expect(requestTo(BrevoCorreoSender.URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "clave"))
                .andExpect(content().json("""
                        {"sender":{"name":"Atlas","email":"app@atlas.test"},
                         "to":[{"email":"ana@correo.com"}],
                         "subject":"Asunto","textContent":"Cuerpo"}
                        """))
                .andRespond(withSuccess());

        sender.enviar("ana@correo.com", "Asunto", "Cuerpo");

        servidor.verify();
    }

    @Test
    void failsLoudlyWhenBrevoRejectsTheEmail() {
        servidor.expect(requestTo(BrevoCorreoSender.URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> sender.enviar("ana@correo.com", "Asunto", "Cuerpo"))
                .isInstanceOf(RestClientResponseException.class);
    }
}
