package co.edu.atlas.ejercicio;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long>, JpaSpecificationExecutor<Ejercicio> {

    /** Filas [categoriaId, total] de fichas publicadas (no eliminadas) de una region. */
    @Query("select e.categoria.id, count(e) from Ejercicio e "
            + "where e.region.id = :regionId and e.estado = co.edu.atlas.ejercicio.EstadoEjercicio.PUBLICADO "
            + "group by e.categoria.id")
    List<Object[]> contarPublicadosPorCategoria(@Param("regionId") Long regionId);

    @Override
    @EntityGraph(attributePaths = {"region", "categoria"})
    Page<Ejercicio> findAll(Specification<Ejercicio> spec, Pageable pageable);
}
