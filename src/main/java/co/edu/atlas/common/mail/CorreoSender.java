package co.edu.atlas.common.mail;

/** Puerto de salida para enviar correos. La implementacion real (Resend/Brevo) se conecta en otra fase. */
public interface CorreoSender {

    void enviar(String destino, String asunto, String cuerpo);
}
