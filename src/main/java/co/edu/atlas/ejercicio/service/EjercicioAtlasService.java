package co.edu.atlas.ejercicio.service;

import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.ejercicio.FiltroEjercicios;
import co.edu.atlas.ejercicio.dto.EjercicioResumenResponse;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta del atlas: solo fichas publicadas (HU-01, HU-02, HU-03). */
@Service
@Transactional(readOnly = true)
public class EjercicioAtlasService {

    private static final Sort POR_NOMBRE = Sort.by("nombre").and(Sort.by("id"));

    private final EjercicioRepository ejercicios;

    public EjercicioAtlasService(EjercicioRepository ejercicios) {
        this.ejercicios = ejercicios;
    }

    public List<EjercicioResumenResponse> listar(Long regionId, Long categoriaId) {
        return ejercicios.findAll(FiltroEjercicios.de(EstadoEjercicio.PUBLICADO, regionId, categoriaId, null), POR_NOMBRE)
                .stream()
                .map(EjercicioAtlasService::resumen)
                .toList();
    }

    private static EjercicioResumenResponse resumen(Ejercicio e) {
        return new EjercicioResumenResponse(
                e.getId(), e.getNombre(), FichaMapper.region(e.getRegion()), FichaMapper.categoria(e.getCategoria()));
    }
}
