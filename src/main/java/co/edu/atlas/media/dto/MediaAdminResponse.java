package co.edu.atlas.media.dto;

import co.edu.atlas.media.MediaEstado;
import co.edu.atlas.media.MediaRol;
import co.edu.atlas.media.MediaTipo;

public record MediaAdminResponse(
        Long id,
        MediaTipo tipo,
        MediaRol rol,
        String contentType,
        long tamanoBytes,
        String url,
        int orden,
        MediaEstado estado) {}
