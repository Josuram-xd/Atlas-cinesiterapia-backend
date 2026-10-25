package co.edu.atlas.ejercicio.dto;

import jakarta.validation.constraints.Size;

public record DosificacionDto(
        @Size(max = 100) String series,
        @Size(max = 100) String repeticiones,
        @Size(max = 100) String intensidad,
        @Size(max = 100) String tiempo,
        @Size(max = 100) String descanso,
        @Size(max = 100) String frecuencia) {}
