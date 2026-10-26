package co.edu.atlas.comentario.service;

import co.edu.atlas.comentario.Comentario;
import co.edu.atlas.comentario.ComentarioRepository;
import co.edu.atlas.comentario.ReporteComentario;
import co.edu.atlas.comentario.ReporteComentarioRepository;
import co.edu.atlas.comentario.dto.ReporteResponse;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.ratelimit.RateLimiter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reporte de comentarios ajenos (HU-25). Un reporte por (usuario, comentario). El comentario reportado sigue
 * visible y entra en la cola de revision del administrador mientras tenga reportes.
 */
@Service
@Transactional
public class ReporteComentarioService {

    private final ComentarioRepository comentarios;
    private final ReporteComentarioRepository reportes;
    private final RateLimiter limiter;

    public ReporteComentarioService(
            ComentarioRepository comentarios, ReporteComentarioRepository reportes, RateLimiter limiter) {
        this.comentarios = comentarios;
        this.reportes = reportes;
        this.limiter = limiter;
    }

    public ReporteResponse reportar(Long comentarioId, Long actorId) {
        if (!limiter.tryAcquire("reportar|" + actorId)) {
            throw ApiException.tooManyRequests("DEMASIADOS_INTENTOS", "Demasiadas acciones seguidas, espera un momento");
        }
        Comentario c = comentarios
                .findById(comentarioId)
                .filter(x -> x.visiblePara(actorId))
                .orElseThrow(() -> ApiException.notFound("COMENTARIO_NO_ENCONTRADO", "El comentario no existe"));
        if (c.esDe(actorId)) {
            throw ApiException.forbidden("REPORTE_PROPIO", "No puedes reportar tu propio comentario");
        }
        if (reportes.existsByComentarioIdAndUsuarioId(comentarioId, actorId)) {
            throw yaReportado();
        }
        try {
            reportes.saveAndFlush(new ReporteComentario(comentarioId, actorId));
        } catch (DataIntegrityViolationException e) {
            throw yaReportado();
        }
        return new ReporteResponse(comentarioId);
    }

    private static ApiException yaReportado() {
        return ApiException.conflict("YA_REPORTADO", "Ya reportaste este comentario");
    }
}
