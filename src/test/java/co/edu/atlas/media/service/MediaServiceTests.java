package co.edu.atlas.media.service;

import static co.edu.atlas.support.Entidades.conId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.auth.support.MutableClock;
import co.edu.atlas.media.AlmacenamientoMedia;
import co.edu.atlas.media.Media;
import co.edu.atlas.media.MediaEstado;
import co.edu.atlas.media.MediaProperties;
import co.edu.atlas.media.MediaRepository;
import co.edu.atlas.media.MediaRol;
import co.edu.atlas.media.MediaTipo;
import co.edu.atlas.media.ObjetoAlmacenado;
import co.edu.atlas.media.SubidaPrefirmada;
import co.edu.atlas.media.dto.MediaAdminResponse;
import co.edu.atlas.media.dto.SolicitudSubida;
import co.edu.atlas.media.dto.SubidaResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediaServiceTests {

    @Mock MediaRepository medias;
    @Mock EjercicioRepository ejercicios;
    @Mock AlmacenamientoMedia almacenamiento;

    MutableClock clock = new MutableClock();
    MediaProperties props = new MediaProperties("fake", Duration.ofMinutes(10), Duration.ofHours(1), "", null);

    private MediaService servicio() {
        return new MediaService(medias, ejercicios, almacenamiento, props, clock);
    }

    private static Ejercicio ficha(EstadoEjercicio estado) {
        Ejercicio e = conId(new Ejercicio("Puente", null, null, 1L), 5L);
        if (estado == EstadoEjercicio.PUBLICADO) {
            e.publicar(Instant.now());
        }
        return e;
    }

    private static Media imagen(String clave) {
        return conId(new Media(5L, MediaTipo.IMAGEN, MediaRol.POSICION_INICIAL, clave, "a.jpg", "image/jpeg", 1000L, 1, Instant.now()), 9L);
    }

    private static SolicitudSubida imagenJpeg() {
        return new SolicitudSubida(MediaTipo.IMAGEN, MediaRol.POSICION_INICIAL, "a.jpg", "image/jpeg", 1000L);
    }

    @Test
    void solicitarSubidaCreaUnaMediaPendienteYDevuelveLaUrl() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));
        when(almacenamiento.prepararSubida(anyString(), eq("image/jpeg"), eq(1000L)))
                .thenReturn(new SubidaPrefirmada("https://r2/put", "PUT", Map.of(), Instant.now()));

        SubidaResponse r = servicio().solicitarSubida(5L, imagenJpeg());

        assertThat(r.urlSubida()).isEqualTo("https://r2/put");
        assertThat(r.metodo()).isEqualTo("PUT");
    }

    @Test
    void solicitarSubidaDescartaLasPendientesVencidas() {
        Media vencida = conId(new Media(5L, MediaTipo.IMAGEN, MediaRol.POSICION_FINAL, "ejercicios/5/v.jpg", "v.jpg",
                "image/jpeg", 10L, 1, Instant.now()), 8L);
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.findByEjercicioIdAndEstadoAndCreatedAtBefore(eq(5L), eq(MediaEstado.PENDIENTE), any(Instant.class)))
                .thenReturn(List.of(vencida));
        when(medias.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));
        when(almacenamiento.prepararSubida(anyString(), anyString(), anyLong()))
                .thenReturn(new SubidaPrefirmada("https://r2/put", "PUT", Map.of(), Instant.now()));

        servicio().solicitarSubida(5L, imagenJpeg());

        verify(almacenamiento).eliminar("ejercicios/5/v.jpg");
        verify(medias).deleteAll(List.of(vencida));
    }

    @Test
    void solicitarSubidaRechazaUnFormatoNoSoportado() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));

        assertThatThrownBy(() -> servicio().solicitarSubida(5L,
                        new SolicitudSubida(MediaTipo.IMAGEN, MediaRol.POSICION_INICIAL, "a.pdf", "application/pdf", 1000L)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_TIPO_NO_SOPORTADO"));
    }

    @Test
    void solicitarImagenSinRolFalla() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));

        assertThatThrownBy(() -> servicio().solicitarSubida(5L,
                        new SolicitudSubida(MediaTipo.IMAGEN, null, "a.jpg", "image/jpeg", 1000L)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_ROL_REQUERIDO"));
    }

    @Test
    void solicitarVideoCuandoYaHayUnoRespetaElLimite() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.contarVigentes(any(), any(), any())).thenReturn(1L);

        assertThatThrownBy(() -> servicio().solicitarSubida(5L,
                        new SolicitudSubida(MediaTipo.VIDEO, null, "v.mp4", "video/mp4", 1000L)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_LIMITE_ALCANZADO"));
    }

    @Test
    void solicitarSubidaDeFichaInexistenteFalla() {
        when(ejercicios.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().solicitarSubida(5L, imagenJpeg()))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("EJERCICIO_NO_ENCONTRADO"));
    }

    @Test
    void confirmarMarcaLaMediaCuandoElObjetoExisteConElMismoTamano() {
        Media m = imagen("ejercicios/5/a.jpg");
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.findByIdAndEjercicioId(9L, 5L)).thenReturn(Optional.of(m));
        when(almacenamiento.informacion("ejercicios/5/a.jpg")).thenReturn(Optional.of(new ObjetoAlmacenado(1000L, "image/jpeg")));

        servicio().confirmar(5L, 9L);

        assertThat(m.getEstado()).isEqualTo(MediaEstado.CONFIRMADO);
    }

    @Test
    void confirmarSinObjetoEnElAlmacenamientoFalla() {
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.findByIdAndEjercicioId(9L, 5L)).thenReturn(Optional.of(imagen("ejercicios/5/a.jpg")));
        when(almacenamiento.informacion(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio().confirmar(5L, 9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_NO_SUBIDA"));
    }

    @Test
    void confirmarConTamanoDistintoDescartaElArchivo() {
        Media m = imagen("ejercicios/5/a.jpg");
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.findByIdAndEjercicioId(9L, 5L)).thenReturn(Optional.of(m));
        when(almacenamiento.informacion("ejercicios/5/a.jpg")).thenReturn(Optional.of(new ObjetoAlmacenado(999L, "image/jpeg")));

        assertThatThrownBy(() -> servicio().confirmar(5L, 9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_TAMANO_INVALIDO"));
        verify(medias).delete(m);
        verify(almacenamiento).eliminar("ejercicios/5/a.jpg");
    }

    @Test
    void confirmarUnaMediaYaConfirmadaEsIdempotente() {
        Media m = imagen("ejercicios/5/a.jpg");
        m.confirmar();
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.findByIdAndEjercicioId(9L, 5L)).thenReturn(Optional.of(m));

        servicio().confirmar(5L, 9L);

        verify(almacenamiento, never()).informacion(anyString());
    }

    @Test
    void eliminarLaUltimaMediaDeUnaFichaPublicadaFalla() {
        Media m = imagen("ejercicios/5/a.jpg");
        m.confirmar();
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.PUBLICADO)));
        when(medias.findByIdAndEjercicioId(9L, 5L)).thenReturn(Optional.of(m));
        when(medias.countByEjercicioIdAndEstado(5L, MediaEstado.CONFIRMADO)).thenReturn(1L);

        assertThatThrownBy(() -> servicio().eliminar(5L, 9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo("MEDIA_REQUERIDA"));
        verify(medias, never()).delete(any());
    }

    @Test
    void eliminarBorraLaFilaYElObjetoAunqueElAlmacenamientoFalle() {
        Media m = imagen("ejercicios/5/a.jpg");
        when(ejercicios.findById(5L)).thenReturn(Optional.of(ficha(EstadoEjercicio.BORRADOR)));
        when(medias.findByIdAndEjercicioId(9L, 5L)).thenReturn(Optional.of(m));
        doThrow(new RuntimeException("R2 caido")).when(almacenamiento).eliminar(anyString());

        servicio().eliminar(5L, 9L);

        verify(medias).delete(m);
        verify(almacenamiento).eliminar("ejercicios/5/a.jpg");
    }

    @Test
    void listarConfirmadosPonenElVideoAntesQueLasImagenes() {
        Media imagen = conId(new Media(5L, MediaTipo.IMAGEN, MediaRol.POSICION_INICIAL, "ejercicios/5/i.jpg", "i.jpg",
                "image/jpeg", 10L, 1, Instant.now()), 1L);
        Media video = conId(new Media(5L, MediaTipo.VIDEO, null, "ejercicios/5/v.mp4", "v.mp4", "video/mp4", 10L, 2,
                Instant.now()), 2L);
        imagen.confirmar();
        video.confirmar();
        when(medias.findByEjercicioIdAndEstadoOrderByOrdenAsc(5L, MediaEstado.CONFIRMADO)).thenReturn(List.of(imagen, video));

        List<MediaAdminResponse> lista = servicio().listarConfirmados(5L);

        assertThat(lista).extracting(MediaAdminResponse::tipo).containsExactly(MediaTipo.VIDEO, MediaTipo.IMAGEN);
    }

    @Test
    void contarConfirmadosDelegaEnElRepositorio() {
        when(medias.countByEjercicioIdAndEstado(5L, MediaEstado.CONFIRMADO)).thenReturn(2L);

        assertThat(servicio().contarConfirmados(5L)).isEqualTo(2L);
    }
}
