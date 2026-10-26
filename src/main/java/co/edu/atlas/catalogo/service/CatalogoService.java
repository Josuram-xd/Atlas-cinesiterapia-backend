package co.edu.atlas.catalogo.service;

import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import co.edu.atlas.catalogo.Vista;
import co.edu.atlas.catalogo.dto.CategoriaConConteoResponse;
import co.edu.atlas.catalogo.dto.CategoriaResponse;
import co.edu.atlas.catalogo.dto.RegionResponse;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.EjercicioRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de los catalogos que arman la navegacion del atlas (HU-01). */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final RegionAnatomicaRepository regiones;
    private final CategoriaTerapeuticaRepository categorias;
    private final EjercicioRepository ejercicios;

    public CatalogoService(
            RegionAnatomicaRepository regiones,
            CategoriaTerapeuticaRepository categorias,
            EjercicioRepository ejercicios) {
        this.regiones = regiones;
        this.categorias = categorias;
        this.ejercicios = ejercicios;
    }

    public List<RegionResponse> regiones(Vista vista) {
        var lista = vista == null ? regiones.findAllByOrderByVistaAscOrdenAsc() : regiones.findByVistaOrderByOrdenAsc(vista);
        return lista.stream().map(RegionResponse::de).toList();
    }

    public List<CategoriaResponse> categorias() {
        return categorias.findAllByOrderByOrdenAsc().stream().map(CategoriaResponse::de).toList();
    }

    public List<CategoriaConConteoResponse> categoriasDeRegion(Long regionId) {
        if (!regiones.existsById(regionId)) {
            throw ApiException.notFound("REGION_NO_ENCONTRADA", "La region anatomica no existe");
        }
        Map<Long, Long> conteos = new HashMap<>();
        for (Object[] fila : ejercicios.contarPublicadosPorCategoria(regionId)) {
            conteos.put((Long) fila[0], (Long) fila[1]);
        }
        return categorias.findAllByOrderByOrdenAsc().stream()
                .map(c -> new CategoriaConConteoResponse(c.getId(), c.getNombre(), c.getOrden(), conteos.getOrDefault(c.getId(), 0L)))
                .toList();
    }
}
