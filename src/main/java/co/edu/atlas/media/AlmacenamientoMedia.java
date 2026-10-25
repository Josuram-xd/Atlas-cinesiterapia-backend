package co.edu.atlas.media;

import java.util.Optional;

/** Puerto de salida hacia el almacenamiento de archivos (Cloudflare R2 en produccion, fake en dev y tests). */
public interface AlmacenamientoMedia {

    /** Emite una URL temporal para subir un objeto con ese tipo y tamano exactos. */
    SubidaPrefirmada prepararSubida(String clave, String contentType, long tamanoBytes);

    /** URL con la que el front muestra el objeto (publica estable o prefirmada temporal). */
    String urlLectura(String clave);

    /** Metadatos del objeto si existe. */
    Optional<ObjetoAlmacenado> informacion(String clave);

    /** Elimina el objeto; no falla si no existe. */
    void eliminar(String clave);
}
