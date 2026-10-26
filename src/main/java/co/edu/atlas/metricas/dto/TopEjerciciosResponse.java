package co.edu.atlas.metricas.dto;

import co.edu.atlas.ejercicio.dto.CategoriaRef;
import co.edu.atlas.ejercicio.dto.RegionRef;
import java.time.LocalDate;
import java.util.List;

/** Ranking de fichas por visualizaciones. {@code desde}/{@code hasta} son null cuando se consulta todo el historico. */
public record TopEjerciciosResponse(LocalDate desde, LocalDate hasta, int limite, List<Item> items) {

    public record Item(Long ejercicioId, String nombre, RegionRef region, CategoriaRef categoria, long visitas) {}
}
