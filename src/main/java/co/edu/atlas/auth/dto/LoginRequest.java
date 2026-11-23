package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") @Size(max = 255) String correo,
        @NotBlank(message = "La contrasena es obligatoria") @MaxUtf8Bytes(72) String password) {}
