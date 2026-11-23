package co.edu.atlas.common.mail;

/** Puerto de salida para enviar correos. En produccion lo implementa Brevo (BrevoCorreoSender). */
public interface CorreoSender {

    void enviar(String destino, String asunto, String cuerpo);
}
