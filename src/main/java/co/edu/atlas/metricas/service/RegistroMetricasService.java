package co.edu.atlas.metricas.service;

import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.metricas.MetricasProperties;
import co.edu.atlas.metricas.SesionUso;
import co.edu.atlas.metricas.SesionUsoRepository;
import co.edu.atlas.metricas.TipoVisita;
import co.edu.atlas.metricas.Visita;
import co.edu.atlas.metricas.VisitaRepository;
import co.edu.atlas.metricas.dto.SesionResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro de visitas (RF_032) y sesiones de uso por ping. */
@Service
@Transactional
public class RegistroMetricasService {

    private final VisitaRepository visitas;
    private final SesionUsoRepository sesiones;
    private final EjercicioRepository ejercicios;
    private final MetricasProperties props;
    private final Clock clock;

    public RegistroMetricasService(
            VisitaRepository visitas,
            SesionUsoRepository sesiones,
            EjercicioRepository ejercicios,
            MetricasProperties props,
            Clock clock) {
        this.visitas = visitas;
        this.sesiones = sesiones;
        this.ejercicios = ejercicios;
        this.props = props;
        this.clock = clock;
    }

    /**
     * Registra una visita del usuario. FICHA exige una ficha publicada; ATLAS no admite ficha. Una repeticion
     * inmediata al mismo destino (doble disparo del front) se ignora sin error.
     */
    public void registrarVisita(Long usuarioId, TipoVisita tipo, Long ejercicioId) {
        validarDestino(tipo, ejercicioId);
        Instant ahora = clock.instant();
        Instant desde = ahora.minus(props.ventanaDuplicados());
        if (visitas.existsByUsuarioIdAndTipoAndEjercicioIdAndCreatedAtAfter(usuarioId, tipo, ejercicioId, desde)) {
            return;
        }
        visitas.save(new Visita(usuarioId, tipo, ejercicioId, ahora));
    }

    /**
     * Ping de actividad. Continua la sesion vigente del usuario o abre una nueva si pasaron mas de
     * {@code inactividad} desde el ultimo ping.
     */
    public SesionResponse ping(Long usuarioId) {
        Instant ahora = clock.instant();
        Optional<SesionUso> ultima = sesiones.findFirstByUsuarioIdOrderByUltimoPingAtDesc(usuarioId);
        if (ultima.isPresent() && ultima.get().vigente(ahora, props.inactividad())) {
            SesionUso s = ultima.get();
            if (Duration.between(s.getUltimoPingAt(), ahora).compareTo(props.intervaloMinimoPing()) >= 0) {
                s.registrarPing(ahora);
            }
            return SesionResponse.de(s, false);
        }
        return SesionResponse.de(sesiones.save(new SesionUso(usuarioId, ahora)), true);
    }

    private void validarDestino(TipoVisita tipo, Long ejercicioId) {
        if (tipo == TipoVisita.ATLAS) {
            if (ejercicioId != null) {
                throw ApiException.badRequest("VISITA_INVALIDA", "Una visita al atlas no lleva ejercicioId");
            }
            return;
        }
        if (ejercicioId == null) {
            throw ApiException.badRequest("VISITA_INVALIDA", "Una visita a una ficha requiere ejercicioId");
        }
        ejercicios
                .findById(ejercicioId)
                .filter(e -> e.getEstado() == EstadoEjercicio.PUBLICADO)
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO", "El ejercicio no existe"));
    }
}
