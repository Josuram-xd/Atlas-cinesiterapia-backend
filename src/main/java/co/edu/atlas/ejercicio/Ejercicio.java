package co.edu.atlas.ejercicio;

import co.edu.atlas.catalogo.CategoriaTerapeutica;
import co.edu.atlas.catalogo.RegionAnatomica;
import co.edu.atlas.common.error.ApiException;
import co.edu.atlas.common.text.TextoBusqueda;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.SQLRestriction;

/** Ficha de ejercicio. El borrado es blando: las filas con deleted_at nunca se consultan. */
@Entity
@Table(name = "ejercicio")
@SQLRestriction("deleted_at IS NULL")
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "nombre_busqueda", nullable = false, length = 200)
    private String nombreBusqueda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionAnatomica region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaTerapeutica categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoEjercicio estado = EstadoEjercicio.BORRADOR;

    @Column(name = "objetivo_terapeutico", length = 5000)
    private String objetivoTerapeutico;

    @Column(name = "estructuras_intervenidas", length = 5000)
    private String estructurasIntervenidas;

    @Column(length = 5000)
    private String indicaciones;

    @Column(name = "precauciones_contraindicaciones", length = 5000)
    private String precaucionesContraindicaciones;

    @Column(name = "posicion_paciente", length = 5000)
    private String posicionPaciente;

    @Column(name = "posicion_fisioterapeuta", length = 5000)
    private String posicionFisioterapeuta;

    @Column(length = 5000)
    private String ejecucion;

    @Column(name = "comandos_verbales", length = 5000)
    private String comandosVerbales;

    @Embedded
    private Dosificacion dosificacion = Dosificacion.vacia();

    @Column(length = 5000)
    private String progresion;

    @Column(name = "errores_comunes", length = 5000)
    private String erroresComunes;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ejercicio_estudiante", joinColumns = @JoinColumn(name = "ejercicio_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "nombre", nullable = false, length = 150)
    private List<String> estudiantesCreadores = new ArrayList<>();

    @Column(length = 10)
    private String cohorte;

    @Column(name = "profesora_revisora", length = 150)
    private String profesoraRevisora;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;

    @Column(name = "actualizado_por")
    private Long actualizadoPor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "publicado_at")
    private Instant publicadoAt;

    @Column(name = "archivado_at")
    private Instant archivadoAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private long version;

    protected Ejercicio() {}

    public Ejercicio(String nombre, RegionAnatomica region, CategoriaTerapeutica categoria, Long creadoPor) {
        this.nombre = nombre.strip();
        this.nombreBusqueda = TextoBusqueda.normalizar(nombre);
        this.region = region;
        this.categoria = categoria;
        this.creadoPor = creadoPor;
    }

    @PrePersist
    void alCrear() {
        Instant ahora = Instant.now();
        createdAt = ahora;
        updatedAt = ahora;
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = Instant.now();
    }

    /** Reemplaza todo el contenido; vacio y espacios se guardan como null. Region o categoria null conservan la actual. */
    public void actualizar(ContenidoFicha c, Long actor) {
        this.nombre = c.nombre().strip();
        this.nombreBusqueda = TextoBusqueda.normalizar(c.nombre());
        if (c.region() != null) {
            this.region = c.region();
        }
        if (c.categoria() != null) {
            this.categoria = c.categoria();
        }
        this.objetivoTerapeutico = Textos.limpiar(c.objetivoTerapeutico());
        this.estructurasIntervenidas = Textos.limpiar(c.estructurasIntervenidas());
        this.indicaciones = Textos.limpiar(c.indicaciones());
        this.precaucionesContraindicaciones = Textos.limpiar(c.precaucionesContraindicaciones());
        this.posicionPaciente = Textos.limpiar(c.posicionPaciente());
        this.posicionFisioterapeuta = Textos.limpiar(c.posicionFisioterapeuta());
        this.ejecucion = Textos.limpiar(c.ejecucion());
        this.comandosVerbales = Textos.limpiar(c.comandosVerbales());
        Dosificacion d = c.dosificacion() == null ? Dosificacion.vacia() : c.dosificacion();
        this.dosificacion = new Dosificacion(
                d.getSeries(), d.getRepeticiones(), d.getIntensidad(), d.getTiempo(), d.getDescanso(), d.getFrecuencia());
        this.progresion = Textos.limpiar(c.progresion());
        this.erroresComunes = Textos.limpiar(c.erroresComunes());
        this.estudiantesCreadores.clear();
        this.estudiantesCreadores.addAll(Textos.limpiarLista(c.estudiantesCreadores()));
        this.cohorte = Textos.limpiar(c.cohorte());
        this.profesoraRevisora = Textos.limpiar(c.profesoraRevisora());
        this.actualizadoPor = actor;
    }

    /** Contenido actual de la ficha. */
    public ContenidoFicha contenido() {
        return new ContenidoFicha(
                nombre, region, categoria, objetivoTerapeutico, estructurasIntervenidas, indicaciones,
                precaucionesContraindicaciones, posicionPaciente, posicionFisioterapeuta, ejecucion, comandosVerbales,
                dosificacion, progresion, erroresComunes, getEstudiantesCreadores(), cohorte, profesoraRevisora);
    }

    public void registrarEditor(Long actor) {
        this.actualizadoPor = actor;
    }

    public void publicar(Instant ahora) {
        exigir(estado == EstadoEjercicio.BORRADOR, "Solo se puede publicar una ficha en borrador");
        estado = EstadoEjercicio.PUBLICADO;
        publicadoAt = ahora;
    }

    public void archivar(Instant ahora) {
        exigir(estado == EstadoEjercicio.PUBLICADO, "Solo se puede archivar una ficha publicada");
        estado = EstadoEjercicio.ARCHIVADO;
        archivadoAt = ahora;
    }

    public void restaurar() {
        exigir(estado == EstadoEjercicio.ARCHIVADO, "Solo se puede restaurar una ficha archivada");
        estado = EstadoEjercicio.PUBLICADO;
        archivadoAt = null;
    }

    public void eliminar(Instant ahora) {
        deletedAt = ahora;
    }

    public boolean estaEliminado() {
        return deletedAt != null;
    }

    private void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw ApiException.conflict("ESTADO_INVALIDO", mensaje);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreBusqueda() {
        return nombreBusqueda;
    }

    public RegionAnatomica getRegion() {
        return region;
    }

    public CategoriaTerapeutica getCategoria() {
        return categoria;
    }

    public EstadoEjercicio getEstado() {
        return estado;
    }

    public String getObjetivoTerapeutico() {
        return objetivoTerapeutico;
    }

    public String getEstructurasIntervenidas() {
        return estructurasIntervenidas;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public String getPrecaucionesContraindicaciones() {
        return precaucionesContraindicaciones;
    }

    public String getPosicionPaciente() {
        return posicionPaciente;
    }

    public String getPosicionFisioterapeuta() {
        return posicionFisioterapeuta;
    }

    public String getEjecucion() {
        return ejecucion;
    }

    public String getComandosVerbales() {
        return comandosVerbales;
    }

    public Dosificacion getDosificacion() {
        return dosificacion;
    }

    public String getProgresion() {
        return progresion;
    }

    public String getErroresComunes() {
        return erroresComunes;
    }

    public List<String> getEstudiantesCreadores() {
        return List.copyOf(estudiantesCreadores);
    }

    public String getCohorte() {
        return cohorte;
    }

    public String getProfesoraRevisora() {
        return profesoraRevisora;
    }

    public Long getCreadoPor() {
        return creadoPor;
    }

    public Long getActualizadoPor() {
        return actualizadoPor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getPublicadoAt() {
        return publicadoAt;
    }

    public Instant getArchivadoAt() {
        return archivadoAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public long getVersion() {
        return version;
    }
}
