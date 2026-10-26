package co.edu.atlas.ejercicio.dto;

import co.edu.atlas.ejercicio.EstadoEjercicio;
import java.time.Instant;

public record EjercicioAdminResumen(
        Long id,
        String nombre,
        EstadoEjercicio estado,
        RegionRef region,
        CategoriaRef categoria,
        UsuarioRef creadoPor,
        Instant actualizadoEn) {}
