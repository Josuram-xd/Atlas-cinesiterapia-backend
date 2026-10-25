package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(
        @NotBlank(message = "El token es obligatorio") String token,
        @NotBlank(message = "La contrasena es obligatoria")
                @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
                @MaxUtf8Bytes(72)
                String nuevaPassword) {}
