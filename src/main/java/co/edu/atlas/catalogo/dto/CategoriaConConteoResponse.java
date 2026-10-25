package co.edu.atlas.catalogo.dto;

/** Categoria con la cantidad de ejercicios publicados en una region (0 = estado vacio). */
public record CategoriaConConteoResponse(Long id, String nombre, int orden, long totalEjercicios) {}
