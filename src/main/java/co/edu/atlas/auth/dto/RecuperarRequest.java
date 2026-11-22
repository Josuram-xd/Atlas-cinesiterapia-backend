package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecuperarRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo invalido") @Size(max = 255) String correo) {}
