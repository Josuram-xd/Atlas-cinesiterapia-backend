package co.edu.atlas.ejercicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.atlas.common.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class EjercicioTests {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-01-02T10:00:00Z");

    private Ejercicio nuevo() {
        return new Ejercicio("Flexión de rodilla", null, null, 1L);
    }

    @Test
    void startsAsDraftWithNormalizedSearchName() {
        Ejercicio e = nuevo();

        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.BORRADOR);
        assertThat(e.getNombreBusqueda()).isEqualTo("flexion de rodilla");
        assertThat(e.getCreadoPor()).isEqualTo(1L);
    }

    @Test
    void updatingContentRefreshesSearchNameAndTrims() {
        Ejercicio e = nuevo();

        e.actualizar(ContenidoFicha.vacio("  Extensión de Codo ", null, null), 2L);

        assertThat(e.getNombre()).isEqualTo("Extensión de Codo");
        assertThat(e.getNombreBusqueda()).isEqualTo("extension de codo");
        assertThat(e.getActualizadoPor()).isEqualTo(2L);
    }

    @Test
    void emptyStringsAreStoredAsNull() {
        Ejercicio e = nuevo();
        ContenidoFicha c = new ContenidoFicha(
                "N", null, null, "  ", " x ", null, null, null, null, null, null,
                new Dosificacion(" ", "3x12", null, null, null, null), null, null,
                List.of(" Ana ", "", "Luis"), " ", null);

        e.actualizar(c, 1L);

        assertThat(e.getObjetivoTerapeutico()).isNull();
        assertThat(e.getEstructurasIntervenidas()).isEqualTo("x");
        assertThat(e.getDosificacion().getSeries()).isNull();
        assertThat(e.getDosificacion().getRepeticiones()).isEqualTo("3x12");
        assertThat(e.getEstudiantesCreadores()).containsExactly("Ana", "Luis");
        assertThat(e.getCohorte()).isNull();
    }

    @Test
    void publishesFromDraftOnly() {
        Ejercicio e = nuevo();

        e.publicar(T0);

        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.PUBLICADO);
        assertThat(e.getPublicadoAt()).isEqualTo(T0);
        assertThatThrownBy(() -> e.publicar(T1))
                .isInstanceOf(ApiException.class)
                .extracting(x -> ((ApiException) x).getCode()).isEqualTo("ESTADO_INVALIDO");
    }

    @Test
    void archivesOnlyPublishedAndRestoresOnlyArchived() {
        Ejercicio e = nuevo();
        assertThatThrownBy(() -> e.archivar(T0)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> e.restaurar()).isInstanceOf(ApiException.class);

        e.publicar(T0);
        e.archivar(T1);
        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.ARCHIVADO);
        assertThat(e.getArchivadoAt()).isEqualTo(T1);

        e.restaurar();
        assertThat(e.getEstado()).isEqualTo(EstadoEjercicio.PUBLICADO);
        assertThat(e.getArchivadoAt()).isNull();
    }

    @Test
    void softDeleteMarksTimestamp() {
        Ejercicio e = nuevo();

        e.eliminar(T1);

        assertThat(e.getDeletedAt()).isEqualTo(T1);
        assertThat(e.estaEliminado()).isTrue();
    }
}
