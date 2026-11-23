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

    /**
     * Filas [ejercicioId, total] de fichas publicadas, de mayor a menor (usa ix_visita_ejercicio_fecha). Las fichas
     * archivadas o eliminadas no entran en el ranking.
     */
    @Query("select e.id, count(v) from Visita v, Ejercicio e where v.ejercicioId = e.id "
            + "and e.estado = co.edu.atlas.ejercicio.EstadoEjercicio.PUBLICADO and v.fecha between :desde and :hasta "
            + "group by e.id order by count(v) desc, e.id asc")
    List<Object[]> masConsultados(
            @Param("desde") java.time.LocalDate desde,
            @Param("hasta") java.time.LocalDate hasta,
            org.springframework.data.domain.Pageable limite);

    /** Anti-duplicados: ya hay una visita reciente del usuario al mismo destino (ejercicioId null = atlas). */
    boolean existsByUsuarioIdAndTipoAndEjercicioIdAndCreatedAtAfter(
            Long usuarioId, TipoVisita tipo, Long ejercicioId, Instant desde);
}
