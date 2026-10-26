package co.edu.atlas.catalogo.dto;

import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.catalogo.Vista;

public record RegionResponse(Long id, String nombre, Vista vista, String svgId, int orden) {

    public static RegionResponse de(RegionAnatomica r) {
        return new RegionResponse(r.getId(), r.getNombre(), r.getVista(), r.getSvgId(), r.getOrden());
    }
}
