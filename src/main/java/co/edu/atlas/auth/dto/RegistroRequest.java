package co.edu.atlas.auth.dto;

import co.edu.atlas.auth.usuario.RolPerfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo invalido") @Size(max = 255) String correo,
        @NotBlank(message = "La contrasena es obligatoria")
                @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
                String password,
        @NotNull(message = "Selecciona un rol") RolPerfil rolPerfil) {}
