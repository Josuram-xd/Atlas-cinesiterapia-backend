package co.edu.atlas.metricas;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    long countByFechaGreaterThanEqual(java.time.LocalDate desde);

    /** Filas [fecha, total] por dia dentro del rango (usa ix_visita_fecha). */
    @Query("select v.fecha, count(v) from Visita v where v.fecha between :desde and :hasta group by v.fecha")
    List<Object[]> contarPorDia(@Param("desde") java.time.LocalDate desde, @Param("hasta") java.time.LocalDate hasta);

    /** Anti-duplicados: ya hay una visita reciente del usuario al mismo destino (ejercicioId null = atlas). */
    boolean existsByUsuarioIdAndTipoAndEjercicioIdAndCreatedAtAfter(
            Long usuarioId, TipoVisita tipo, Long ejercicioId, Instant desde);
}
