package co.edu.atlas.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "Escribe un comentario") @Size(max = 1000, message = "Maximo 1000 caracteres") String texto) {}
