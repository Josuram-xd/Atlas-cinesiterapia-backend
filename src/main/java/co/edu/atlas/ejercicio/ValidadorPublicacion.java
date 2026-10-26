package co.edu.atlas.ejercicio;

import co.edu.atlas.common.error.ApiError.FieldViolation;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla de campos obligatorios para publicar (RF_025). Opcionales: progresion y errores comunes.
 * Los nombres de campo coinciden con los del cuerpo de la API. Nombre, region y categoria no se revisan aqui:
 * son obligatorios desde que se crea el borrador (NOT NULL y validacion de entrada).
 */
public final class ValidadorPublicacion {

    static final String OBLIGATORIO = "Campo obligatorio";
    static final String MEDIA_REQUERIDA = "Agrega al menos una imagen o un video";

    private ValidadorPublicacion() {}

    /** Campos pendientes de la ficha tal como esta guardada. */
    public static List<FieldViolation> camposFaltantes(Ejercicio e, long mediosConfirmados) {
        return camposFaltantes(e.contenido(), mediosConfirmados);
    }

    /** Lista los campos pendientes en un orden estable; vacia si el contenido puede publicarse. */
    public static List<FieldViolation> camposFaltantes(ContenidoFicha c, long mediosConfirmados) {
        List<FieldViolation> faltantes = new ArrayList<>();
        texto(faltantes, "objetivoTerapeutico", c.objetivoTerapeutico());
        texto(faltantes, "estructurasIntervenidas", c.estructurasIntervenidas());
        texto(faltantes, "indicaciones", c.indicaciones());
        texto(faltantes, "precaucionesContraindicaciones", c.precaucionesContraindicaciones());
        texto(faltantes, "posicionPaciente", c.posicionPaciente());
        texto(faltantes, "posicionFisioterapeuta", c.posicionFisioterapeuta());
        texto(faltantes, "ejecucion", c.ejecucion());
        texto(faltantes, "comandosVerbales", c.comandosVerbales());
        Dosificacion d = c.dosificacion() == null ? Dosificacion.vacia() : c.dosificacion();
        texto(faltantes, "dosificacion.series", d.getSeries());
        texto(faltantes, "dosificacion.repeticiones", d.getRepeticiones());
        texto(faltantes, "dosificacion.intensidad", d.getIntensidad());
        texto(faltantes, "dosificacion.tiempo", d.getTiempo());
        texto(faltantes, "dosificacion.descanso", d.getDescanso());
        texto(faltantes, "dosificacion.frecuencia", d.getFrecuencia());
        if (mediosConfirmados < 1) {
            faltantes.add(new FieldViolation("media", MEDIA_REQUERIDA));
        }
        boolean hayEstudiantes = c.estudiantesCreadores() != null
                && c.estudiantesCreadores().stream().anyMatch(n -> n != null && !n.isBlank());
        requerido(faltantes, "estudiantesCreadores", hayEstudiantes);
        texto(faltantes, "cohorte", c.cohorte());
        texto(faltantes, "profesoraRevisora", c.profesoraRevisora());
        return faltantes;
    }

    private static void texto(List<FieldViolation> faltantes, String campo, String valor) {
        requerido(faltantes, campo, valor != null && !valor.isBlank());
    }

    private static void requerido(List<FieldViolation> faltantes, String campo, boolean presente) {
        if (!presente) {
            faltantes.add(new FieldViolation(campo, OBLIGATORIO));
        }
    }
}
