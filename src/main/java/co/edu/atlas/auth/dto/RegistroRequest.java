package co.edu.atlas.auth.dto;

import co.edu.atlas.auth.usuario.RolPerfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El correo es obligatorio")
                @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "Correo invalido")
                @Size(max = 254) String correo,
        @NotBlank(message = "La contrasena es obligatoria")
                @Size(max = 72, message = "La contrasena debe tener maximo 72 caracteres")
                @PasswordSegura
                @MaxUtf8Bytes(72)
                String password,
        @NotNull(message = "Selecciona un rol") RolPerfil rolPerfil) {}
