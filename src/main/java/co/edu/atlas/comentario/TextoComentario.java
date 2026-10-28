package co.edu.atlas.comentario;

import co.edu.atlas.common.error.ApiException;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Saneamiento del texto de un comentario. El texto es siempre texto plano: se eliminan caracteres de control,
 * invisibles y de direccion (usados para evadir el filtro o falsear el texto) y se rechaza cualquier marcado
 * HTML. El front debe renderizarlo como texto, nunca como HTML.
 */
public final class TextoComentario {

    private static final Pattern MARCADO = Pattern.compile("<\\s*[a-zA-Z/!?]");
    private static final Pattern SALTOS = Pattern.compile("\n{3,}");

    private TextoComentario() {}

    public static String limpiar(String bruto) {
        if (bruto == null) {
            throw invalido("El comentario no puede estar vacio");
        }
        String t = Normalizer.normalize(bruto, Normalizer.Form.NFC).replace("\r\n", "\n").replace('\r', '\n').replace('\t', ' ');
        StringBuilder sb = new StringBuilder(t.length());
        t.codePoints().forEach(cp -> {
            int tipo = Character.getType(cp);
            boolean descartar = (cp != '\n' && tipo == Character.CONTROL) || tipo == Character.FORMAT;
            if (!descartar) {
                sb.appendCodePoint(cp);
            }
        });
        String limpio = SALTOS.matcher(sb.toString().strip()).replaceAll("\n\n");
        if (limpio.isEmpty()) {
            throw invalido("El comentario no puede estar vacio");
        }
        if (limpio.length() > Comentario.MAX_TEXTO) {
            throw invalido("El comentario supera los " + Comentario.MAX_TEXTO + " caracteres");
        }
        if (MARCADO.matcher(limpio).find()) {
            throw invalido("El comentario no puede contener codigo ni etiquetas HTML");
        }
        return limpio;
    }

    private static ApiException invalido(String mensaje) {
        return ApiException.badRequest("TEXTO_INVALIDO", mensaje);
    }
}
