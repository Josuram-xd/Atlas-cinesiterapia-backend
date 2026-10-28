package co.edu.atlas.ejercicio.dto;

import co.edu.atlas.media.dto.MediaResponse;
import java.time.Instant;
import java.util.List;

/** Ficha completa de un ejercicio publicado (HU-02). No expone estado ni autoria interna. */
public record EjercicioResponse(
        Long id,
        String nombre,
        RegionRef region,
        CategoriaRef categoria,
        String objetivoTerapeutico,
        String estructurasIntervenidas,
        String indicaciones,
        String precaucionesContraindicaciones,
        String posicionPaciente,
        String posicionFisioterapeuta,
        String ejecucion,
        String comandosVerbales,
        DosificacionDto dosificacion,
        String progresion,
        String erroresComunes,
        List<String> estudiantesCreadores,
        String cohorte,
        String profesoraRevisora,
        MediaResponse video,
        List<MediaResponse> imagenes,
        Instant actualizadoEn) {}
