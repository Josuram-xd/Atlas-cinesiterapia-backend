package co.edu.atlas.catalogo.dto;

import co.edu.atlas.catalogo.CategoriaTerapeutica;

public record CategoriaResponse(Long id, String nombre, int orden) {

    public static CategoriaResponse de(CategoriaTerapeutica c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getOrden());
    }
}
