package co.edu.atlas.ejercicio.service;

import static co.edu.atlas.support.Entidades.conId;
import static co.edu.atlas.support.Entidades.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResumen;
import co.edu.atlas.ejercicio.dto.EjercicioRequest;
import co.edu.atlas.media.service.MediaService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EjercicioAdminServiceTests {

    @Mock EjercicioRepository ejercicios;
    @Mock RegionAnatomicaRepository regiones;
    @Mock CategoriaTerapeuticaRepository categorias;
    @Mock UsuarioRepository usuarios;
    @Mock MediaService media;

    MutableClock clock = new MutableClock();

    private EjercicioAdminService servicio() {
        return new EjercicioAdminService(ejercicios, regiones, categorias, usuarios, media, clock);
    }

    /** Solo nombre, region y categoria: la ficha queda incompleta. */
    private static EjercicioRequest sinDatos() {
        return new EjercicioRequest(
                "Puente", 1L, 2L, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /** Ficha real con region y categoria mock (sus constructores son protegidos). */
    private static Ejercicio ficha(long id, EstadoEjercicio estado) {
        Ejercicio e = conId(new Ejercicio("Puente", mock(RegionAnatomica.class), mock(CategoriaTerapeutica.class), 1L), id);
        if (estado == EstadoEjercicio.PUBLICADO) {
            e.publicar(Instant.now());
        }
        return e;
    }

    @Test
    void crearGuardaUnBorradorDelAutor() {
        when(regiones.findById(1L)).thenReturn(Optional.of(mock(RegionAnatomica.class)));
        when(categorias.findById(2L)).thenReturn(Optional.of(mock(CategoriaTerapeutica.class)));
        when(ejercicios.saveAndFlush(any(Ejercicio.class))).thenAnswer(i -> i.getArgument(0));

        servicio().crear(sinDatos(), 7L);

        ArgumentCaptor<Ejercicio> guardado = ArgumentCaptor.forClass(Ejercicio.class);
        verify(ejercicios).saveAndFlush(guardado.capture());
        assertThat(guardado.getValue().getNombre()).isEqualTo("Puente");
        assertThat(guardado.getValue().getEstado()).isEqualTo(EstadoEjercicio.BORRADOR);
        assertThat(guardado.getValue().getCreadoPor()).isEqualTo(7L);
    }

    @Test
    void crearConRegionInexistenteFalla() {
        when(regiones.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().crear(sinDatos(), 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("REGION_INVALIDA"));
        verify(ejercicios, never()).saveAndFlush(any());
    }

    @Test
    void crearConCategoriaInexistenteFalla() {
        when(regiones.findById(1L)).thenReturn(Optional.of(mock(RegionAnatomica.class)));
        when(categorias.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().crear(sinDatos(), 7L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("CATEGORIA_INVALIDA"));
    }

    @Test
    void publicarUnaFichaIncompletaDevuelve422ConLosCamposPendientes() {
        Ejercicio e = ficha(5L, EstadoEjercicio.BORRADOR);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(e));

        assertThatThrownBy(() -> servicio().publicar(5L, 2L))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("FICHA_INCOMPLETA");
                    assertThat(ex.getStatus().value()).isEqualTo(422);
                });
        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.BORRADOR);
    }

    @Test
    void actualizarUnaFichaPublicadaConDatosIncompletosFallaSinMutar() {
        Ejercicio e = ficha(5L, EstadoEjercicio.PUBLICADO);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(e));
        when(regiones.findById(1L)).thenReturn(Optional.of(mock(RegionAnatomica.class)));
        when(categorias.findById(2L)).thenReturn(Optional.of(mock(CategoriaTerapeutica.class)));

        assertThatThrownBy(() -> servicio().actualizar(5L, sinDatos(), 2L))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("FICHA_INCOMPLETA"));
        verify(ejercicios, never()).saveAndFlush(any());
    }

    @Test
    void archivarYRestaurarRegistranQuienEditó() {
        Ejercicio e = ficha(5L, EstadoEjercicio.PUBLICADO);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(e));
        when(ejercicios.saveAndFlush(any(Ejercicio.class))).thenAnswer(i -> i.getArgument(0));

        servicio().archivar(5L, 2L);
        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.ARCHIVADO);
        assertThat(e.getActualizadoPor()).isEqualTo(2L);

        servicio().restaurar(5L, 3L);
        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.PUBLICADO);
        assertThat(e.getActualizadoPor()).isEqualTo(3L);
    }

    @Test
    void eliminarOcultaLaFichaParaTodaLaApi() {
        Ejercicio e = ficha(5L, EstadoEjercicio.BORRADOR);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(e));

        servicio().eliminar(5L, 2L);

        assertThat(e.estaEliminado()).isTrue();
        assertThatThrownBy(() -> servicio().obtener(5L))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
    }

    @Test
    void listarDevuelveLasFichasPaginadas() {
        when(ejercicios.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ficha(5L, EstadoEjercicio.BORRADOR))));
        when(usuarios.findAllById(anyIterable())).thenReturn(List.of(usuario(1L)));

        Pagina<EjercicioAdminResumen> pagina = servicio().listar(null, null, null, null, 0, 20);

        assertThat(pagina.content()).extracting(EjercicioAdminResumen::nombre).containsExactly("Puente");
    }
}
