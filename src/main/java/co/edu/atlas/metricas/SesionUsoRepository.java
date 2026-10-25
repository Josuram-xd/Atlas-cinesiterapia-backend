package co.edu.atlas.metricas;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionUsoRepository extends JpaRepository<SesionUso, Long> {

    Optional<SesionUso> findFirstByUsuarioIdOrderByUltimoPingAtDesc(Long usuarioId);

    /**
     * Una fila [promedio de duracionSegundos (null si no hay), total] de las sesiones iniciadas en
     * [{@code inicio}, {@code finExclusivo}) con duracion registrada (mas de un ping). Usa ix_sesion_inicio.
     */
    @Query("select avg(s.duracionSegundos), count(s) from SesionUso s "
            + "where s.inicioAt >= :inicio and s.inicioAt < :finExclusivo and s.duracionSegundos > 0")
    List<Object[]> promedioDuracion(@Param("inicio") Instant inicio, @Param("finExclusivo") Instant finExclusivo);
}
