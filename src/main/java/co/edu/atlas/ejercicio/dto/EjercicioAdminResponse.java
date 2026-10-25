package co.edu.atlas.ejercicio.dto;

import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.media.dto.MediaAdminResponse;
import java.time.Instant;
import java.util.List;

/** Ficha completa para administracion (cualquier estado). */
public record EjercicioAdminResponse(
        Long id,
        EstadoEjercicio estado,
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
        List<MediaAdminResponse> media,
        UsuarioRef creadoPor,
        UsuarioRef actualizadoPor,
        Instant creadoEn,
        Instant actualizadoEn,
        Instant publicadoEn,
        Instant archivadoEn,
        List<String> camposFaltantes,
        long version) {}
