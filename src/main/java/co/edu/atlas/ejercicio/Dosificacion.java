package co.edu.atlas.ejercicio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Dosificacion en texto libre ("3x12", "hasta fatiga"). */
@Embeddable
public class Dosificacion {

    @Column(name = "dosis_series", length = 100)
    private String series;

    @Column(name = "dosis_repeticiones", length = 100)
    private String repeticiones;

    @Column(name = "dosis_intensidad", length = 100)
    private String intensidad;

    @Column(name = "dosis_tiempo", length = 100)
    private String tiempo;

    @Column(name = "dosis_descanso", length = 100)
    private String descanso;

    @Column(name = "dosis_frecuencia", length = 100)
    private String frecuencia;

    protected Dosificacion() {}

    public Dosificacion(
            String series, String repeticiones, String intensidad, String tiempo, String descanso, String frecuencia) {
        this.series = Textos.limpiar(series);
        this.repeticiones = Textos.limpiar(repeticiones);
        this.intensidad = Textos.limpiar(intensidad);
        this.tiempo = Textos.limpiar(tiempo);
        this.descanso = Textos.limpiar(descanso);
        this.frecuencia = Textos.limpiar(frecuencia);
    }

    public static Dosificacion vacia() {
        return new Dosificacion(null, null, null, null, null, null);
    }

    public String getSeries() {
        return series;
    }

    public String getRepeticiones() {
        return repeticiones;
    }

    public String getIntensidad() {
        return intensidad;
    }

    public String getTiempo() {
        return tiempo;
    }

    public String getDescanso() {
        return descanso;
    }

    public String getFrecuencia() {
        return frecuencia;
    }
}
