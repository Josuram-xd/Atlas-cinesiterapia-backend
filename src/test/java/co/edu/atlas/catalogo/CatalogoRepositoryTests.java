package co.edu.atlas.catalogo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CatalogoRepositoryTests {

    @Autowired
    RegionAnatomicaRepository regiones;

    @Autowired
    CategoriaTerapeuticaRepository categorias;

    @Test
    void seedsTheNineCategoriesInOrder() {
        List<CategoriaTerapeutica> todas = categorias.findAllByOrderByOrdenAsc();

        assertThat(todas).hasSize(9);
        assertThat(todas.get(0).getNombre()).isEqualTo("Movilización pasiva");
        assertThat(todas.get(8).getNombre()).isEqualTo("Facilitación neuromuscular propioceptiva (FNP)");
    }

    @Test
    void seedsThirteenAnteriorAndFourteenPosteriorRegions() {
        assertThat(regiones.findByVistaOrderByOrdenAsc(Vista.ANTERIOR)).hasSize(13);
        assertThat(regiones.findByVistaOrderByOrdenAsc(Vista.POSTERIOR)).hasSize(14);
    }

    @Test
    void everySvgIdCarriesTheViewPrefix() {
        assertThat(regiones.findAll()).allSatisfy(r ->
                assertThat(r.getSvgId()).startsWith(r.getVista().name().toLowerCase() + "-"));
    }

    @Test
    void svgIdsMatchTheFrontContract() {
        assertThat(regiones.findAll().stream().map(RegionAnatomica::getSvgId))
                .containsExactlyInAnyOrder(
                        "anterior-cabeza-cuello", "anterior-hombro", "anterior-brazo", "anterior-codo",
                        "anterior-antebrazo", "anterior-muneca-mano", "anterior-torax", "anterior-abdomen",
                        "anterior-cadera", "anterior-muslo", "anterior-rodilla", "anterior-pierna",
                        "anterior-tobillo-pie", "posterior-cabeza-cuello", "posterior-cervical",
                        "posterior-hombro-escapula", "posterior-brazo", "posterior-codo", "posterior-antebrazo",
                        "posterior-muneca-mano", "posterior-dorsal", "posterior-lumbar", "posterior-gluteo",
                        "posterior-muslo", "posterior-rodilla", "posterior-pierna", "posterior-tobillo-pie");
    }

    @Test
    void findsRegionBySvgId() {
        assertThat(regiones.findBySvgId("posterior-lumbar")).get()
                .extracting(RegionAnatomica::getNombre).isEqualTo("Columna lumbar");
    }
}
