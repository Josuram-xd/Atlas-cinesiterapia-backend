package co.edu.atlas.ejercicio.service;

import static co.edu.atlas.support.Entidades.conId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.dto.EjercicioResponse;
import co.edu.atlas.ejercicio.dto.EjercicioResumenResponse;
import co.edu.atlas.media.MediaEstado;
import co.edu.atlas.media.MediaRol;
import co.edu.atlas.media.MediaTipo;
import co.edu.atlas.media.dto.MediaAdminResponse;
import co.edu.atlas.media.service.MediaService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EjercicioAtlasServiceTests {

    @Mock EjercicioRepository ejercicios;
    @Mock MediaService media;

    private EjercicioAtlasService servicio() {
        return new EjercicioAtlasService(ejercicios, media);
    }

    /** Ficha publicada real; region y categoria son mocks porque sus constructores son protegidos. */
    private static Ejercicio publicada(long id, String nombre) {
        Ejercicio e = conId(new Ejercicio(nombre, mock(RegionAnatomica.class), mock(CategoriaTerapeutica.class), 1L), id);
        e.publicar(Instant.now());
        return e;
    }

    @Test
    void listarDevuelveElResumenDeLasFichasEncontradas() {
        when(ejercicios.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(publicada(5L, "Puente")));

        assertThat(servicio().listar(null, null)).extracting(EjercicioResumenResponse::nombre).containsExactly("Puente");
    }

    @Test
    void buscarDevuelveResultadosPaginados() {
        when(ejercicios.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(publicada(5L, "Puente"))));

        assertThat(servicio().buscar("puente")).extracting(EjercicioResumenResponse::id).containsExactly(5L);
    }

    @Test
    void obtenerFichaSinMediosNoTieneVideoNiImagenes() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(publicada(5L, "Puente")));
        when(media.listarConfirmados(5L)).thenReturn(List.of());

        EjercicioResponse r = servicio().obtener(5L);

        assertThat(r.nombre()).isEqualTo("Puente");
        assertThat(r.video()).isNull();
        assertThat(r.imagenes()).isEmpty();
    }

    @Test
    void obtenerSeparaElVideoDeLasImagenes() {
        MediaAdminResponse video = new MediaAdminResponse(
                1L, MediaTipo.VIDEO, null, "video/mp4", 100L, "https://r2/v", 0, MediaEstado.CONFIRMADO);
        MediaAdminResponse imagen = new MediaAdminResponse(
                2L, MediaTipo.IMAGEN, MediaRol.POSICION_INICIAL, "image/png", 50L, "https://r2/i", 1, MediaEstado.CONFIRMADO);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(publicada(5L, "Puente")));
        when(media.listarConfirmados(5L)).thenReturn(List.of(video, imagen));

        EjercicioResponse r = servicio().obtener(5L);

        assertThat(r.video()).isNotNull();
        assertThat(r.imagenes()).hasSize(1);
    }

    @Test
    void obtenerFichaNoPublicadaFallaComoNoEncontrada() {
        Ejercicio borrador = conId(new Ejercicio("Puente", mock(RegionAnatomica.class), mock(CategoriaTerapeutica.class), 1L), 5L);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(borrador));

        assertThatThrownBy(() -> servicio().obtener(5L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
    }
}
