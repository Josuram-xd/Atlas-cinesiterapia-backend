package co.edu.atlas.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Archivo (video o imagen) asociado a una ficha. El binario vive en el almacenamiento, aqui solo su clave. */
@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ejercicio_id", nullable = false)
    private Long ejercicioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MediaTipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MediaRol rol;

    @Column(nullable = false, unique = true, length = 300)
    private String clave;

    @Column(name = "nombre_archivo", length = 200)
    private String nombreArchivo;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private MediaEstado estado = MediaEstado.PENDIENTE;

    @Column(nullable = false)
    private int orden;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Media() {}

    public Media(
            Long ejercicioId,
            MediaTipo tipo,
            MediaRol rol,
            String clave,
            String nombreArchivo,
            String contentType,
            long tamanoBytes,
            int orden,
            Instant ahora) {
        this.ejercicioId = ejercicioId;
        this.tipo = tipo;
        this.rol = rol;
        this.clave = clave;
        this.nombreArchivo = nombreArchivo;
        this.contentType = contentType;
        this.tamanoBytes = tamanoBytes;
        this.orden = orden;
        this.createdAt = ahora;
    }

    public void confirmar() {
        this.estado = MediaEstado.CONFIRMADO;
    }

    public boolean estaConfirmado() {
        return estado == MediaEstado.CONFIRMADO;
    }

    public Long getId() {
        return id;
    }

    public Long getEjercicioId() {
        return ejercicioId;
    }

    public MediaTipo getTipo() {
        return tipo;
    }

    public MediaRol getRol() {
        return rol;
    }

    public String getClave() {
        return clave;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getContentType() {
        return contentType;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public MediaEstado getEstado() {
        return estado;
    }

    public int getOrden() {
        return orden;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
