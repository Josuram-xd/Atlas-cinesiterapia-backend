package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RecuperarRequest(
        @NotBlank(message = "El correo es obligatorio")
                @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "Correo invalido")
                @Size(max = 254) String correo) {}
