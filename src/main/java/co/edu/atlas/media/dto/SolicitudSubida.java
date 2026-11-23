package co.edu.atlas.media.dto;

import co.edu.atlas.media.MediaRol;
import co.edu.atlas.media.MediaTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SolicitudSubida(
        @NotNull(message = "El tipo es obligatorio") MediaTipo tipo,
        MediaRol rol,
        @Size(max = 200, message = "Maximo 200 caracteres") String nombreArchivo,
        @NotBlank(message = "El contentType es obligatorio") @Size(max = 100) String contentType,
        @NotNull(message = "El tamano es obligatorio") @Positive(message = "El tamano debe ser mayor a 0") Long tamanoBytes) {}
