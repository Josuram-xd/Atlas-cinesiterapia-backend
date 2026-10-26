package co.edu.atlas.media;

import co.edu.atlas.common.error.ApiException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Reglas de tipo, tamano y cantidad de los archivos de una ficha (HU-08). */
public final class ReglasMedia {

    public static final long MAX_VIDEO_BYTES = 25L * 1024 * 1024;
    public static final long MAX_IMAGEN_BYTES = 5L * 1024 * 1024;
    public static final int MAX_VIDEOS = 1;
    public static final int MAX_IMAGENES = 8;

    private static final Set<String> TIPOS_VIDEO = Set.of("video/mp4", "video/webm", "image/gif");
    private static final Set<String> TIPOS_IMAGEN = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSIONES = Map.of(
            "video/mp4", "mp4",
            "video/webm", "webm",
            "image/gif", "gif",
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private ReglasMedia() {}

    public static void validar(MediaTipo tipo, String contentType, long tamanoBytes) {
        String ct = normalizar(contentType);
        Set<String> permitidos = tipo == MediaTipo.VIDEO ? TIPOS_VIDEO : TIPOS_IMAGEN;
        if (!permitidos.contains(ct)) {
            String formatos = tipo == MediaTipo.VIDEO ? "MP4, WebM o GIF" : "JPG, PNG o WebP";
            throw ApiException.badRequest("MEDIA_TIPO_NO_SOPORTADO", "Formato no soportado. Usa " + formatos);
        }
        if (tamanoBytes <= 0) {
            throw ApiException.badRequest("MEDIA_VACIA", "El archivo esta vacio");
        }
        long maximo = tipo == MediaTipo.VIDEO ? MAX_VIDEO_BYTES : MAX_IMAGEN_BYTES;
        if (tamanoBytes > maximo) {
            throw ApiException.badRequest(
                    "MEDIA_DEMASIADO_GRANDE", "El archivo supera el maximo de " + (maximo / (1024 * 1024)) + " MB");
        }
    }

    public static String extension(String contentType) {
        return EXTENSIONES.get(normalizar(contentType));
    }

    public static int limite(MediaTipo tipo) {
        return tipo == MediaTipo.VIDEO ? MAX_VIDEOS : MAX_IMAGENES;
    }

    /** Minusculas y sin parametros (por ejemplo "; charset"). */
    public static String normalizar(String contentType) {
        if (contentType == null) {
            return "";
        }
        int corte = contentType.indexOf(';');
        String base = corte >= 0 ? contentType.substring(0, corte) : contentType;
        return base.strip().toLowerCase(Locale.ROOT);
    }
}
