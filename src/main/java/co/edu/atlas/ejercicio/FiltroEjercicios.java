package co.edu.atlas.ejercicio;

import co.edu.atlas.common.text.TextoBusqueda;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Filtros opcionales del listado de administracion; solo se agregan los predicados recibidos. */
public final class FiltroEjercicios {

    private static final char ESCAPE = '\\';

    private FiltroEjercicios() {}

    public static Specification<Ejercicio> de(EstadoEjercicio estado, Long regionId, Long categoriaId, String q) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (estado != null) {
                ps.add(cb.equal(root.get("estado"), estado));
            }
            if (regionId != null) {
                ps.add(cb.equal(root.get("region").get("id"), regionId));
            }
            if (categoriaId != null) {
                ps.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }
            String buscado = TextoBusqueda.normalizar(q);
            if (!buscado.isEmpty()) {
                ps.add(cb.like(root.get("nombreBusqueda"), "%" + TextoBusqueda.escaparLike(buscado) + "%", ESCAPE));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
    }
}
