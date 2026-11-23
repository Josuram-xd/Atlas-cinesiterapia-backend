package co.edu.atlas.ejercicio.dto;

/** Elemento de listado del atlas (solo fichas publicadas). */
public record EjercicioResumenResponse(Long id, String nombre, RegionRef region, CategoriaRef categoria) {}
