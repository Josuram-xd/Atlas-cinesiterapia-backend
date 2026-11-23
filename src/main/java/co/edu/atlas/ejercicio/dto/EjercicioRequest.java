package co.edu.atlas.ejercicio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Contenido de una ficha. Solo nombre, region y categoria son obligatorios para guardar un borrador. */
public record EjercicioRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 200, message = "Maximo 200 caracteres") String nombre,
        @NotNull(message = "Selecciona una region anatomica") Long regionId,
        @NotNull(message = "Selecciona una categoria terapeutica") Long categoriaId,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String objetivoTerapeutico,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String estructurasIntervenidas,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String indicaciones,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String precaucionesContraindicaciones,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String posicionPaciente,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String posicionFisioterapeuta,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String ejecucion,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String comandosVerbales,
        @Valid DosificacionDto dosificacion,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String progresion,
        @Size(max = 5000, message = "Maximo 5000 caracteres") String erroresComunes,
        @Size(max = 20, message = "Maximo 20 estudiantes")
                List<@Size(max = 150, message = "Maximo 150 caracteres") String> estudiantesCreadores,
        @Pattern(regexp = "^(\\d{4}-\\d{1,2})?$", message = "Formato AAAA-N, por ejemplo 2025-1") String cohorte,
        @Size(max = 150, message = "Maximo 150 caracteres") String profesoraRevisora) {}
