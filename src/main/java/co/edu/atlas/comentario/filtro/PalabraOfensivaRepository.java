package co.edu.atlas.comentario.filtro;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PalabraOfensivaRepository extends JpaRepository<PalabraOfensiva, Long> {

    @Query("select p.palabraNormalizada from PalabraOfensiva p")
    List<String> todasNormalizadas();

    Optional<PalabraOfensiva> findByPalabraNormalizada(String palabraNormalizada);

    boolean existsByPalabraNormalizada(String palabraNormalizada);

    Page<PalabraOfensiva> findByPalabraNormalizadaContaining(String texto, Pageable pageable);
}
