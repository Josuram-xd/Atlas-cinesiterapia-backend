package co.edu.atlas.ejercicio;

import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.RegionAnatomica;
import java.util.List;

/** Contenido editable de una ficha (todo salvo estado, autoria y medios). */
public record ContenidoFicha(
        String nombre,
        RegionAnatomica region,
        CategoriaTerapeutica categoria,
        String objetivoTerapeutico,
        String estructurasIntervenidas,
        String indicaciones,
        String precaucionesContraindicaciones,
        String posicionPaciente,
        String posicionFisioterapeuta,
        String ejecucion,
        String comandosVerbales,
        Dosificacion dosificacion,
        String progresion,
        String erroresComunes,
        List<String> estudiantesCreadores,
        String cohorte,
        String profesoraRevisora) {

    public static ContenidoFicha vacio(String nombre, RegionAnatomica region, CategoriaTerapeutica categoria) {
        return new ContenidoFicha(
                nombre, region, categoria, null, null, null, null, null, null, null, null,
                Dosificacion.vacia(), null, null, List.of(), null, null);
    }
}
