package co.edu.atlas.common.text;

import java.text.Normalizer;
import java.util.Locale;

/** Normalizacion de texto para busquedas insensibles a mayusculas y tildes. */
public final class TextoBusqueda {

    private TextoBusqueda() {}

    /** Minusculas, sin diacriticos y con espacios colapsados. Nunca devuelve null. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    /** Escapa los comodines de LIKE (usar con ESCAPE de barra invertida). */
    public static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
