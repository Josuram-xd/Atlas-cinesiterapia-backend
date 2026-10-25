package co.edu.atlas.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PalabraRequest(
        @NotBlank(message = "Escribe una palabra") @Size(max = 60, message = "Maximo 60 caracteres") String palabra) {}
