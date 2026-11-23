package co.edu.atlas.catalogo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionAnatomicaRepository extends JpaRepository<RegionAnatomica, Long> {

    List<RegionAnatomica> findByVistaOrderByOrdenAsc(Vista vista);

    List<RegionAnatomica> findAllByOrderByVistaAscOrdenAsc();

    Optional<RegionAnatomica> findBySvgId(String svgId);
}
