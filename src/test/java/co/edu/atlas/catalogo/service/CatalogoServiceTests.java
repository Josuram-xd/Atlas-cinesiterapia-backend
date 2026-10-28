package co.edu.atlas.catalogo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import co.edu.atlas.catalogo.Vista;
import co.edu.atlas.catalogo.dto.CategoriaConConteoResponse;
import co.edu.atlas.catalogo.dto.RegionResponse;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.EjercicioRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTests {

    @Mock RegionAnatomicaRepository regiones;
    @Mock CategoriaTerapeuticaRepository categorias;
    @Mock EjercicioRepository ejercicios;

    private CatalogoService servicio() {
        return new CatalogoService(regiones, categorias, ejercicios);
    }

    private static RegionAnatomica region(long id) {
        RegionAnatomica r = mock(RegionAnatomica.class);
        when(r.getId()).thenReturn(id);
        return r;
    }

    private static CategoriaTerapeutica categoria(long id, String nombre) {
        CategoriaTerapeutica c = mock(CategoriaTerapeutica.class);
        when(c.getId()).thenReturn(id);
        when(c.getNombre()).thenReturn(nombre);
        return c;
    }

    @Test
    void regionesSinVistaDevuelveTodasEnOrden() {
        RegionAnatomica r = region(1L);
        when(regiones.findAllByOrderByVistaAscOrdenAsc()).thenReturn(List.of(r));

        assertThat(servicio().regiones(null)).extracting(RegionResponse::id).containsExactly(1L);
    }

    @Test
    void regionesDeUnaVistaUsanSuPropioFiltro() {
        when(regiones.findByVistaOrderByOrdenAsc(Vista.POSTERIOR)).thenReturn(List.of());

        assertThat(servicio().regiones(Vista.POSTERIOR)).isEmpty();
    }

    @Test
    void categoriasSeDevuelvenEnOrden() {
        CategoriaTerapeutica c = categoria(2L, "Movilizacion pasiva");
        when(categorias.findAllByOrderByOrdenAsc()).thenReturn(List.of(c));

        assertThat(servicio().categorias()).extracting(co.edu.atlas.catalogo.dto.CategoriaResponse::nombre)
                .containsExactly("Movilizacion pasiva");
    }

    @Test
    void categoriasDeRegionCuentanSoloLasPublicadas() {
        CategoriaTerapeutica activo = categoria(2L, "Activo");
        CategoriaTerapeutica isometrico = categoria(3L, "Isometrico");
        when(regiones.existsById(1L)).thenReturn(true);
        when(ejercicios.contarPublicadosPorCategoria(1L)).thenReturn(List.<Object[]>of(new Object[] {2L, 4L}));
        when(categorias.findAllByOrderByOrdenAsc()).thenReturn(List.of(activo, isometrico));

        assertThat(servicio().categoriasDeRegion(1L))
                .extracting(CategoriaConConteoResponse::totalEjercicios)
                .containsExactly(4L, 0L);
    }

    @Test
    void categoriasDeRegionInexistenteFalla() {
        when(regiones.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> servicio().categoriasDeRegion(9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("REGION_NO_ENCONTRADA"));
    }
}
