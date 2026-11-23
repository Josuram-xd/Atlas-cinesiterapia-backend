package co.edu.atlas.common.web;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Pagina de resultados con la forma estable que consume el front. */
public record Pagina<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> Pagina<T> de(Page<E> page, Function<E, T> mapeo) {
        return new Pagina<>(
                page.getContent().stream().map(mapeo).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
