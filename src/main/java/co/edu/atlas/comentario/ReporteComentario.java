package co.edu.atlas.comentario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/** Reporte de un usuario sobre un comentario ajeno; unico por (usuario, comentario). */
@Entity
@Table(name = "reporte_comentario")
public class ReporteComentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comentario_id", nullable = false)
    private Long comentarioId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReporteComentario() {}

    public ReporteComentario(Long comentarioId, Long usuarioId) {
        this.comentarioId = comentarioId;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getComentarioId() {
        return comentarioId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
