package co.edu.atlas.metricas;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionUsoRepository extends JpaRepository<SesionUso, Long> {

    Optional<SesionUso> findFirstByUsuarioIdOrderByUltimoPingAtDesc(Long usuarioId);
}
