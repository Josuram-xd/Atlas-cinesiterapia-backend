package co.edu.atlas.ejercicio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long>, JpaSpecificationExecutor<Ejercicio> {

    @Override
    @EntityGraph(attributePaths = {"region", "categoria"})
    Page<Ejercicio> findAll(Specification<Ejercicio> spec, Pageable pageable);
}
