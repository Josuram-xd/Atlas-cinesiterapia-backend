package co.edu.atlas.ejercicio.service;

import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.ejercicio.FiltroEjercicios;
import co.edu.atlas.ejercicio.dto.EjercicioResponse;
import co.edu.atlas.ejercicio.dto.EjercicioResumenResponse;
import co.edu.atlas.media.MediaTipo;
import co.edu.atlas.media.dto.MediaResponse;
import co.edu.atlas.media.service.MediaService;
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
    private final MediaService media;

    public EjercicioAtlasService(EjercicioRepository ejercicios, MediaService media) {
        this.ejercicios = ejercicios;
        this.media = media;
    }

    public List<EjercicioResumenResponse> listar(Long regionId, Long categoriaId) {
        return ejercicios.findAll(FiltroEjercicios.de(EstadoEjercicio.PUBLICADO, regionId, categoriaId, null), POR_NOMBRE)
                .stream()
                .map(EjercicioAtlasService::resumen)
                .toList();
    }

    public EjercicioResponse obtener(Long id) {
        Ejercicio e = ejercicios
                .findById(id)
                .filter(x -> !x.estaEliminado() && x.getEstado() == EstadoEjercicio.PUBLICADO)
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO", "El ejercicio no existe"));
        List<MediaResponse> medios =
                media.listarConfirmados(id).stream().map(MediaResponse::de).toList();
        MediaResponse video = medios.stream().filter(m -> m.tipo() == MediaTipo.VIDEO).findFirst().orElse(null);
        List<MediaResponse> imagenes =
                medios.stream().filter(m -> m.tipo() == MediaTipo.IMAGEN).toList();
        return new EjercicioResponse(
                e.getId(), e.getNombre(), FichaMapper.region(e.getRegion()), FichaMapper.categoria(e.getCategoria()),
                e.getObjetivoTerapeutico(), e.getEstructurasIntervenidas(), e.getIndicaciones(),
                e.getPrecaucionesContraindicaciones(), e.getPosicionPaciente(), e.getPosicionFisioterapeuta(),
                e.getEjecucion(), e.getComandosVerbales(), FichaMapper.dosificacion(e.getDosificacion()),
                e.getProgresion(), e.getErroresComunes(), e.getEstudiantesCreadores(), e.getCohorte(),
                e.getProfesoraRevisora(), video, imagenes, e.getUpdatedAt());
    }

    private static EjercicioResumenResponse resumen(Ejercicio e) {
        return new EjercicioResumenResponse(
                e.getId(), e.getNombre(), FichaMapper.region(e.getRegion()), FichaMapper.categoria(e.getCategoria()));
    }
}
