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

    /** Lista los campos pendientes en un orden estable; vacia si la ficha puede publicarse. */
    public static List<FieldViolation> camposFaltantes(Ejercicio e, long mediosConfirmados) {
        List<FieldViolation> faltantes = new ArrayList<>();
        texto(faltantes, "objetivoTerapeutico", e.getObjetivoTerapeutico());
        texto(faltantes, "estructurasIntervenidas", e.getEstructurasIntervenidas());
        texto(faltantes, "indicaciones", e.getIndicaciones());
        texto(faltantes, "precaucionesContraindicaciones", e.getPrecaucionesContraindicaciones());
        texto(faltantes, "posicionPaciente", e.getPosicionPaciente());
        texto(faltantes, "posicionFisioterapeuta", e.getPosicionFisioterapeuta());
        texto(faltantes, "ejecucion", e.getEjecucion());
        texto(faltantes, "comandosVerbales", e.getComandosVerbales());
        Dosificacion d = e.getDosificacion();
        texto(faltantes, "dosificacion.series", d.getSeries());
        texto(faltantes, "dosificacion.repeticiones", d.getRepeticiones());
        texto(faltantes, "dosificacion.intensidad", d.getIntensidad());
        texto(faltantes, "dosificacion.tiempo", d.getTiempo());
        texto(faltantes, "dosificacion.descanso", d.getDescanso());
        texto(faltantes, "dosificacion.frecuencia", d.getFrecuencia());
        if (mediosConfirmados < 1) {
            faltantes.add(new FieldViolation("media", MEDIA_REQUERIDA));
        }
        requerido(faltantes, "estudiantesCreadores", !e.getEstudiantesCreadores().isEmpty());
        texto(faltantes, "cohorte", e.getCohorte());
        texto(faltantes, "profesoraRevisora", e.getProfesoraRevisora());
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
