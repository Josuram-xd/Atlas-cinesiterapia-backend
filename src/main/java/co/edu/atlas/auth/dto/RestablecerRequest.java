package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(
        @NotBlank(message = "El token es obligatorio") @Size(max = 100) String token,
        @NotBlank(message = "La contrasena es obligatoria")
                @Size(max = 72, message = "La contrasena debe tener maximo 72 caracteres")
                @PasswordSegura
                @MaxUtf8Bytes(72)
                String nuevaPassword) {}
