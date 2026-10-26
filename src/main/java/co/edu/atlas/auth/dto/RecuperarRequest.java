package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo invalido") String correo) {}
