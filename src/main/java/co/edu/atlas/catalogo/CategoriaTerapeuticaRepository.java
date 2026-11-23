package co.edu.atlas.catalogo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaTerapeuticaRepository extends JpaRepository<CategoriaTerapeutica, Long> {

    List<CategoriaTerapeutica> findAllByOrderByOrdenAsc();
}
