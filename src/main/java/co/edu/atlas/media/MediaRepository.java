package co.edu.atlas.media;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByEjercicioIdAndEstadoOrderByOrdenAsc(Long ejercicioId, MediaEstado estado);

    Optional<Media> findByIdAndEjercicioId(Long id, Long ejercicioId);

    long countByEjercicioIdAndEstado(Long ejercicioId, MediaEstado estado);

    List<Media> findByEjercicioIdAndEstadoAndCreatedAtBefore(Long ejercicioId, MediaEstado estado, Instant antes);

    /** Cuenta los confirmados y los pendientes aun vigentes (creados despues de {@code desde}). */
    @Query("select count(m) from Media m where m.ejercicioId = :ejercicioId and m.tipo = :tipo "
            + "and (m.estado = co.edu.atlas.media.MediaEstado.CONFIRMADO or m.createdAt > :desde)")
    long contarVigentes(
            @Param("ejercicioId") Long ejercicioId, @Param("tipo") MediaTipo tipo, @Param("desde") Instant desde);

    @Query("select coalesce(max(m.orden), 0) from Media m where m.ejercicioId = :ejercicioId")
    int maxOrden(@Param("ejercicioId") Long ejercicioId);
}
