package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.atlas.common.error.ApiError.FieldViolation;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorPublicacionTests {

    private ContenidoFicha completo() {
        return new ContenidoFicha(
                "Flexión", null, null, "obj", "est", "ind", "prec", "pac", "fis", "eje", "cmd",
                new Dosificacion("3", "12", "baja", "5 s", "30 s", "diaria"), null, null,
                List.of("Ana"), "2025-1", "Dra. Pérez");
    }

    private Ejercicio ficha(ContenidoFicha c) {
        Ejercicio e = new Ejercicio("Flexión", null, null, 1L);
        e.actualizar(c, 1L);
        return e;
    }

    private List<String> campos(Ejercicio e, long medios) {
        return ValidadorPublicacion.camposFaltantes(e, medios).stream().map(FieldViolation::field).toList();
    }

    @Test
    void completeSheetWithMediaHasNoMissingFields() {
        assertThat(campos(ficha(completo()), 1)).isEmpty();
    }

    @Test
    void optionalFieldsMayBeEmpty() {
        ContenidoFicha c = completo();
        assertThat(c.progresion()).isNull();
        assertThat(c.erroresComunes()).isNull();
        assertThat(campos(ficha(c), 3)).isEmpty();
    }

    @Test
    void emptyDraftReportsEveryRequiredField() {
        Ejercicio e = new Ejercicio("Solo nombre", null, null, 1L);

        assertThat(campos(e, 0))
                .containsExactly(
                        "objetivoTerapeutico", "estructurasIntervenidas", "indicaciones",
                        "precaucionesContraindicaciones", "posicionPaciente", "posicionFisioterapeuta", "ejecucion",
                        "comandosVerbales", "dosificacion.series", "dosificacion.repeticiones",
                        "dosificacion.intensidad", "dosificacion.tiempo", "dosificacion.descanso",
                        "dosificacion.frecuencia", "media", "estudiantesCreadores", "cohorte", "profesoraRevisora");
    }

    @Test
    void reportsEachDosageFieldIndividually() {
        ContenidoFicha c = new ContenidoFicha(
                "Flexión", null, null, "obj", "est", "ind", "prec", "pac", "fis", "eje", "cmd",
                new Dosificacion("3", null, "baja", null, "30 s", "diaria"), null, null,
                List.of("Ana"), "2025-1", "Dra. Pérez");

        assertThat(campos(ficha(c), 1)).containsExactly("dosificacion.repeticiones", "dosificacion.tiempo");
    }

    @Test
    void requiresAtLeastOneConfirmedMedia() {
        assertThat(campos(ficha(completo()), 0)).containsExactly("media");
    }

    @Test
    void whitespaceOnlyTextCountsAsMissing() {
        ContenidoFicha c = new ContenidoFicha(
                "Flexión", null, null, "   ", "est", "ind", "prec", "pac", "fis", "eje", "cmd",
                new Dosificacion("3", "12", "baja", "5 s", "30 s", "diaria"), null, null,
                List.of("Ana"), "2025-1", "Dra. Pérez");

        assertThat(campos(ficha(c), 1)).containsExactly("objetivoTerapeutico");
    }

    @Test
    void blankStudentNamesDoNotCount() {
        ContenidoFicha c = new ContenidoFicha(
                "Flexión", null, null, "obj", "est", "ind", "prec", "pac", "fis", "eje", "cmd",
                new Dosificacion("3", "12", "baja", "5 s", "30 s", "diaria"), null, null,
                List.of(" ", ""), "2025-1", "Dra. Pérez");

        assertThat(campos(ficha(c), 1)).containsExactly("estudiantesCreadores");
    }
}
