package co.edu.atlas.ejercicio.service;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.ejercicio.Dosificacion;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.dto.CategoriaRef;
import co.edu.atlas.ejercicio.dto.DosificacionDto;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResponse;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResumen;
import co.edu.atlas.ejercicio.dto.RegionRef;
import co.edu.atlas.ejercicio.dto.UsuarioRef;
import co.edu.atlas.media.dto.MediaAdminResponse;
import java.util.List;
import java.util.Map;

/** Conversiones entre entidades y DTOs de fichas. */
final class FichaMapper {

    private FichaMapper() {}

    static RegionRef region(RegionAnatomica r) {
        return new RegionRef(r.getId(), r.getNombre(), r.getVista(), r.getSvgId());
    }

    static CategoriaRef categoria(CategoriaTerapeutica c) {
        return new CategoriaRef(c.getId(), c.getNombre());
    }

    static UsuarioRef usuario(Map<Long, Usuario> porId, Long id) {
        if (id == null) {
            return null;
        }
        Usuario u = porId.get(id);
        return new UsuarioRef(id, u == null ? null : u.getCorreo());
    }

    static DosificacionDto dosificacion(Dosificacion d) {
        return new DosificacionDto(
                d.getSeries(), d.getRepeticiones(), d.getIntensidad(), d.getTiempo(), d.getDescanso(), d.getFrecuencia());
    }

    static EjercicioAdminResumen resumen(Ejercicio e, Map<Long, Usuario> usuarios) {
        return new EjercicioAdminResumen(
                e.getId(), e.getNombre(), e.getEstado(), region(e.getRegion()), categoria(e.getCategoria()),
                usuario(usuarios, e.getCreadoPor()), e.getUpdatedAt());
    }

    static EjercicioAdminResponse admin(
            Ejercicio e, Map<Long, Usuario> usuarios, List<MediaAdminResponse> media, List<String> camposFaltantes) {
        return new EjercicioAdminResponse(
                e.getId(), e.getEstado(), e.getNombre(), region(e.getRegion()), categoria(e.getCategoria()),
                e.getObjetivoTerapeutico(), e.getEstructurasIntervenidas(), e.getIndicaciones(),
                e.getPrecaucionesContraindicaciones(), e.getPosicionPaciente(), e.getPosicionFisioterapeuta(),
                e.getEjecucion(), e.getComandosVerbales(), dosificacion(e.getDosificacion()), e.getProgresion(),
                e.getErroresComunes(), e.getEstudiantesCreadores(), e.getCohorte(), e.getProfesoraRevisora(),
                media, usuario(usuarios, e.getCreadoPor()), usuario(usuarios, e.getActualizadoPor()), e.getCreatedAt(),
                e.getUpdatedAt(), e.getPublicadoAt(), e.getArchivadoAt(), camposFaltantes, e.getVersion());
    }
}
