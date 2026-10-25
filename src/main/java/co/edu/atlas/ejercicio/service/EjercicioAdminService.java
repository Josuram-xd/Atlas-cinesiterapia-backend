package co.edu.atlas.ejercicio.service;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.auth.usuario.UsuarioRepository;
import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.CategoriaTerapeuticaRepository;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.catalogo.RegionAnatomicaRepository;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.web.Pagina;
import co.edu.atlas.ejercicio.ContenidoFicha;
import co.edu.atlas.ejercicio.Dosificacion;
import co.edu.atlas.ejercicio.Ejercicio;
import co.edu.atlas.ejercicio.EjercicioRepository;
import co.edu.atlas.ejercicio.EstadoEjercicio;
import co.edu.atlas.ejercicio.FiltroEjercicios;
import co.edu.atlas.ejercicio.dto.DosificacionDto;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResponse;
import co.edu.atlas.ejercicio.dto.EjercicioAdminResumen;
import co.edu.atlas.ejercicio.dto.EjercicioRequest;
import co.edu.atlas.media.service.MediaService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion de fichas por administradores. Todos los administradores operan sobre todas las fichas (HU-15). */
@Service
public class EjercicioAdminService {

    private final EjercicioRepository ejercicios;
    private final RegionAnatomicaRepository regiones;
    private final CategoriaTerapeuticaRepository categorias;
    private final UsuarioRepository usuarios;
    private final MediaService media;

    public EjercicioAdminService(
            EjercicioRepository ejercicios,
            RegionAnatomicaRepository regiones,
            CategoriaTerapeuticaRepository categorias,
            UsuarioRepository usuarios,
            MediaService media) {
        this.ejercicios = ejercicios;
        this.regiones = regiones;
        this.categorias = categorias;
        this.usuarios = usuarios;
        this.media = media;
    }

    @Transactional
    public EjercicioAdminResponse crear(EjercicioRequest request, Long actorId) {
        ContenidoFicha contenido = contenido(request);
        Ejercicio e = new Ejercicio(contenido.nombre(), contenido.region(), contenido.categoria(), actorId);
        e.actualizar(contenido, actorId);
        return respuesta(ejercicios.saveAndFlush(e));
    }

    @Transactional(readOnly = true)
    public EjercicioAdminResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public EjercicioAdminResponse actualizar(Long id, EjercicioRequest request, Long actorId) {
        Ejercicio e = buscar(id);
        e.actualizar(contenido(request), actorId);
        return respuesta(ejercicios.saveAndFlush(e));
    }

    @Transactional(readOnly = true)
    public Pagina<EjercicioAdminResumen> listar(
            EstadoEjercicio estado, Long regionId, Long categoriaId, String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by("id")));
        Page<Ejercicio> resultado = ejercicios.findAll(FiltroEjercicios.de(estado, regionId, categoriaId, q), pageable);
        Map<Long, Usuario> autores =
                usuariosDe(resultado.getContent().stream().map(Ejercicio::getCreadoPor).toList());
        return Pagina.de(resultado, e -> FichaMapper.resumen(e, autores));
    }

    Ejercicio buscar(Long id) {
        return ejercicios
                .findById(id)
                .orElseThrow(() -> ApiException.notFound("EJERCICIO_NO_ENCONTRADO", "La ficha no existe"));
    }

    EjercicioAdminResponse respuesta(Ejercicio e) {
        Set<Long> ids = new HashSet<>();
        ids.add(e.getCreadoPor());
        if (e.getActualizadoPor() != null) {
            ids.add(e.getActualizadoPor());
        }
        return FichaMapper.admin(e, usuariosDe(ids), media.listarConfirmados(e.getId()));
    }

    private Map<Long, Usuario> usuariosDe(Collection<Long> ids) {
        Set<Long> unicos = new HashSet<>(ids);
        if (unicos.isEmpty()) {
            return Map.of();
        }
        return usuarios.findAllById(unicos).stream().collect(Collectors.toMap(Usuario::getId, Function.identity()));
    }

    private ContenidoFicha contenido(EjercicioRequest r) {
        RegionAnatomica region = regiones
                .findById(r.regionId())
                .orElseThrow(() -> ApiException.badRequest("REGION_INVALIDA", "La region anatomica no existe"));
        CategoriaTerapeutica categoria = categorias
                .findById(r.categoriaId())
                .orElseThrow(() -> ApiException.badRequest("CATEGORIA_INVALIDA", "La categoria terapeutica no existe"));
        DosificacionDto d = r.dosificacion();
        Dosificacion dosis = d == null
                ? Dosificacion.vacia()
                : new Dosificacion(d.series(), d.repeticiones(), d.intensidad(), d.tiempo(), d.descanso(), d.frecuencia());
        return new ContenidoFicha(
                r.nombre(), region, categoria, r.objetivoTerapeutico(), r.estructurasIntervenidas(), r.indicaciones(),
                r.precaucionesContraindicaciones(), r.posicionPaciente(), r.posicionFisioterapeuta(), r.ejecucion(),
                r.comandosVerbales(), dosis, r.progresion(), r.erroresComunes(), r.estudiantesCreadores(), r.cohorte(),
                r.profesoraRevisora());
    }
}
