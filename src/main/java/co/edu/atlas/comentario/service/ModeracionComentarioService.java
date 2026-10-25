package co.edu.atlas.comentario.service;

import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.ComentarioRepository;
import co.edu.atlas.comentario.ReporteComentarioRepository;
import co.edu.atlas.comentario.TipoRevision;
import co.edu.atlas.comentario.dto.ComentarioRevisionResponse;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Moderacion de comentarios por administradores: cola de revision (HU-26). */
@Service
@Transactional
public class ModeracionComentarioService {

    private final ComentarioRepository comentarios;
    private final ReporteComentarioRepository reportes;
    private final EjercicioRepository ejercicios;

    public ModeracionComentarioService(
            ComentarioRepository comentarios, ReporteComentarioRepository reportes, EjercicioRepository ejercicios) {
        this.comentarios = comentarios;
        this.reportes = reportes;
        this.ejercicios = ejercicios;
    }

    /** Ocultados automaticamente y/o reportados, el mas antiguo primero. {@code tipo} null devuelve ambos. */
    @Transactional(readOnly = true)
    public Pagina<ComentarioRevisionResponse> cola(TipoRevision tipo, int page, int size) {
        boolean ocultados = tipo == null || tipo == TipoRevision.OCULTADOS;
        boolean reportados = tipo == null || tipo == TipoRevision.REPORTADOS;
        Page<Comentario> pagina = comentarios.colaRevision(ocultados, reportados, PageRequest.of(page, size));
        List<Comentario> filas = pagina.getContent();
        Map<Long, Long> totales = totalesDeReportes(filas.stream().map(Comentario::getId).toList());
        Map<Long, String> nombres = nombresDeFichas(filas.stream().map(Comentario::getEjercicioId).distinct().toList());
        return Pagina.de(
                pagina, c -> ComentarioRevisionResponse.de(c, nombres.get(c.getEjercicioId()), totales.getOrDefault(c.getId(), 0L)));
    }

    private Map<Long, Long> totalesDeReportes(Collection<Long> ids) {
        Map<Long, Long> totales = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] fila : reportes.contarPorComentario(ids)) {
                totales.put((Long) fila[0], (Long) fila[1]);
            }
        }
        return totales;
    }

    private Map<Long, String> nombresDeFichas(Collection<Long> ids) {
        return ejercicios.findAllById(ids).stream().collect(Collectors.toMap(Ejercicio::getId, Ejercicio::getNombre));
    }
}
