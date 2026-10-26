package co.edu.atlas.media.dto;

import co.edu.atlas.media.MediaRol;
import co.edu.atlas.media.MediaTipo;

public record MediaResponse(
        Long id, MediaTipo tipo, MediaRol rol, String contentType, long tamanoBytes, String url, int orden) {

    public static MediaResponse de(MediaAdminResponse m) {
        return new MediaResponse(m.id(), m.tipo(), m.rol(), m.contentType(), m.tamanoBytes(), m.url(), m.orden());
    }
}
