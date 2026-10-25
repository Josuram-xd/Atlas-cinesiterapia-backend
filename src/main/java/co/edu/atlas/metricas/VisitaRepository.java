package co.edu.atlas.metricas;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    long countByFechaGreaterThanEqual(java.time.LocalDate desde);

    /** Anti-duplicados: ya hay una visita reciente del usuario al mismo destino (ejercicioId null = atlas). */
    boolean existsByUsuarioIdAndTipoAndEjercicioIdAndCreatedAtAfter(
            Long usuarioId, TipoVisita tipo, Long ejercicioId, Instant desde);
}
