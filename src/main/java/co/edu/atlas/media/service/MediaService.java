package co.edu.atlas.media.service;

import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.media.AlmacenamientoMedia;
import co.edu.atlas.media.Media;
import co.edu.atlas.media.MediaEstado;
import co.edu.atlas.media.MediaProperties;
import co.edu.atlas.media.MediaRepository;
import co.edu.atlas.media.MediaRol;
import co.edu.atlas.media.MediaTipo;
import co.edu.atlas.media.ObjetoAlmacenado;
import co.edu.atlas.media.ReglasMedia;
import co.edu.atlas.media.SubidaPrefirmada;
import co.edu.atlas.media.dto.MediaAdminResponse;
import co.edu.atlas.media.dto.SolicitudSubida;
import co.edu.atlas.media.dto.SubidaResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ciclo de subida de archivos: URL prefirmada, confirmacion y eliminacion (HU-08). */
@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final MediaRepository medias;
    private final EjercicioRepository ejercicios;
    private final AlmacenamientoMedia almacenamiento;
    private final MediaProperties props;
    private final Clock clock;

    public MediaService(
            MediaRepository medias,
            EjercicioRepository ejercicios,
            AlmacenamientoMedia almacenamiento,
            MediaProperties props,
            Clock clock) {
        this.medias = medias;
        this.ejercicios = ejercicios;
        this.almacenamiento = almacenamiento;
        this.props = props;
        this.clock = clock;
    }

    @Transactional
    public SubidaResponse solicitarSubida(Long ejercicioId, SolicitudSubida req) {
        exigirFicha(ejercicioId);
        ReglasMedia.validar(req.tipo(), req.contentType(), req.tamanoBytes());
        MediaRol rol = rolPara(req);
        Instant ahora = clock.instant();
        Instant desde = ahora.minus(props.subidaTtl());

        descartarPendientesVencidas(ejercicioId, desde);
        if (medias.contarVigentes(ejercicioId, req.tipo(), desde) >= ReglasMedia.limite(req.tipo())) {
            throw ApiException.conflict(
                    "MEDIA_LIMITE_ALCANZADO",
                    req.tipo() == MediaTipo.VIDEO
                            ? "La ficha ya tiene un video; elimina el actual para subir otro"
                            : "La ficha ya tiene el maximo de " + ReglasMedia.MAX_IMAGENES + " imagenes");
        }

        String contentType = ReglasMedia.normalizar(req.contentType());
        String clave = "ejercicios/" + ejercicioId + "/" + UUID.randomUUID() + "." + ReglasMedia.extension(contentType);
        Media media = medias.save(new Media(
                ejercicioId, req.tipo(), rol, clave, req.nombreArchivo(), contentType, req.tamanoBytes(),
                medias.maxOrden(ejercicioId) + 1, ahora));
        SubidaPrefirmada subida = almacenamiento.prepararSubida(clave, contentType, req.tamanoBytes());
        return new SubidaResponse(media.getId(), subida.url(), subida.metodo(), subida.cabeceras(), subida.expiraEn());
    }

    /** Idempotente. Si el objeto no esta o su tamano no coincide, no se confirma (y en el segundo caso se descarta). */
    @Transactional(noRollbackFor = ApiException.class)
    public MediaAdminResponse confirmar(Long ejercicioId, Long mediaId) {
        exigirFicha(ejercicioId);
        Media media = buscar(ejercicioId, mediaId);
        if (media.estaConfirmado()) {
            return respuesta(media);
        }
        ObjetoAlmacenado objeto = almacenamiento
                .informacion(media.getClave())
                .orElseThrow(() -> ApiException.conflict(
                        "MEDIA_NO_SUBIDA", "El archivo todavia no esta en el almacenamiento; sube el archivo e intenta de nuevo"));
        if (objeto.tamanoBytes() != media.getTamanoBytes()) {
            medias.delete(media);
            borrarObjeto(media.getClave());
            throw ApiException.conflict("MEDIA_TAMANO_INVALIDO", "El archivo subido no coincide con el tamano declarado");
        }
        media.confirmar();
        return respuesta(media);
    }

    @Transactional
    public void eliminar(Long ejercicioId, Long mediaId) {
        Ejercicio ficha = exigirFicha(ejercicioId);
        Media media = buscar(ejercicioId, mediaId);
        boolean ultimoRequerido = media.estaConfirmado()
                && ficha.getEstado() != EstadoEjercicio.BORRADOR
                && medias.countByEjercicioIdAndEstado(ejercicioId, MediaEstado.CONFIRMADO) <= 1;
        if (ultimoRequerido) {
            throw ApiException.conflict(
                    "MEDIA_REQUERIDA", "Una ficha publicada o archivada necesita al menos una imagen o un video");
        }
        medias.delete(media);
        borrarObjeto(media.getClave());
    }

    /** Medios confirmados: el video primero y luego las imagenes por orden. */
    @Transactional(readOnly = true)
    public List<MediaAdminResponse> listarConfirmados(Long ejercicioId) {
        return medias.findByEjercicioIdAndEstadoOrderByOrdenAsc(ejercicioId, MediaEstado.CONFIRMADO).stream()
                .sorted(Comparator.comparing((Media m) -> m.getTipo() == MediaTipo.VIDEO ? 0 : 1)
                        .thenComparingInt(Media::getOrden))
                .map(this::respuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarConfirmados(Long ejercicioId) {
        return medias.countByEjercicioIdAndEstado(ejercicioId, MediaEstado.CONFIRMADO);
    }

    private MediaRol rolPara(SolicitudSubida req) {
        if (req.tipo() == MediaTipo.VIDEO) {
            return null;
        }
        if (req.rol() == null) {
            throw ApiException.badRequest("MEDIA_ROL_REQUERIDO", "Indica el rol de la imagen (posicion inicial, final, toma, otro)");
        }
        return req.rol();
    }

    private void descartarPendientesVencidas(Long ejercicioId, Instant desde) {
        List<Media> vencidas =
                medias.findByEjercicioIdAndEstadoAndCreatedAtBefore(ejercicioId, MediaEstado.PENDIENTE, desde);
        vencidas.forEach(m -> borrarObjeto(m.getClave()));
        medias.deleteAll(vencidas);
        medias.flush();
    }

    private void borrarObjeto(String clave) {
        try {
            almacenamiento.eliminar(clave);
        } catch (RuntimeException e) {
            log.warn("No se pudo eliminar el objeto {} del almacenamiento", clave, e);
        }
    }

    private Ejercicio exigirFicha(Long ejercicioId) {
        return ejercicios
                .findById(ejercicioId)
                .filter(e -> !e.estaEliminado())
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO", "La ficha no existe"));
    }

    private Media buscar(Long ejercicioId, Long mediaId) {
        return medias.findByIdAndEjercicioId(mediaId, ejercicioId)
                .orElseThrow(() -> ApiException.notFound("MEDIA_NO_ENCONTRADA", "El archivo no existe en esta ficha"));
    }

    private MediaAdminResponse respuesta(Media m) {
        return new MediaAdminResponse(
                m.getId(), m.getTipo(), m.getRol(), m.getContentType(), m.getTamanoBytes(),
                almacenamiento.urlLectura(m.getClave()), m.getOrden(), m.getEstado());
    }
}
