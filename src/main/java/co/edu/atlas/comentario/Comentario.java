package co.edu.atlas.comentario;

import co.edu.atlas.auth.usuario.Usuario;
import co.edu.atlas.common.error.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Comentario de una ficha. Hilos de un nivel: {@code padreId} apunta siempre a un comentario raiz.
 * Al eliminarse se conserva la fila (estado ELIMINADO) pero se vacia el texto.
 */
@Entity
@Table(name = "comentario")
public class Comentario {

    public static final int MAX_TEXTO = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ejercicio_id", nullable = false)
    private Long ejercicioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "padre_id")
    private Long padreId;

    @Column(nullable = false, length = MAX_TEXTO)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoComentario estado = EstadoComentario.PUBLICADO;

    @Column(name = "editado_at")
    private Instant editadoAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Comentario() {}

    public Comentario(Long ejercicioId, Usuario autor, Long padreId, String texto, EstadoComentario estado) {
        this.ejercicioId = ejercicioId;
        this.autor = autor;
        this.padreId = padreId;
        this.texto = texto;
        this.estado = estado;
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

    public boolean esRespuesta() {
        return padreId != null;
    }

    public boolean estaEliminado() {
        return estado == EstadoComentario.ELIMINADO;
    }

    public boolean esDe(Long usuarioId) {
        return autor.getId().equals(usuarioId);
    }

    /** Lo ve su autor si esta publicado o en revision; los demas, solo si esta publicado. */
    public boolean visiblePara(Long usuarioId) {
        return estado == EstadoComentario.PUBLICADO || (estado == EstadoComentario.EN_REVISION && esDe(usuarioId));
    }

    /** Reemplaza el texto; el estado resultante lo decide el filtro (HU-22: la edicion vuelve a filtrarse). */
    public void editar(String nuevoTexto, EstadoComentario nuevoEstado, Instant ahora) {
        exigirNoEliminado();
        this.texto = nuevoTexto;
        this.estado = nuevoEstado;
        this.editadoAt = ahora;
    }

    public void eliminar(Instant ahora) {
        exigirNoEliminado();
        this.estado = EstadoComentario.ELIMINADO;
        this.texto = "";
        this.deletedAt = ahora;
    }

    /** Vuelve a PUBLICADO (aceptacion del admin). Un comentario eliminado no se puede aceptar. */
    public void aceptar() {
        exigirNoEliminado();
        this.estado = EstadoComentario.PUBLICADO;
    }

    private void exigirNoEliminado() {
        if (estaEliminado()) {
            throw ApiException.conflict("COMENTARIO_ELIMINADO", "El comentario ya fue eliminado");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getEjercicioId() {
        return ejercicioId;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Long getPadreId() {
        return padreId;
    }

    public String getTexto() {
        return texto;
    }

    public EstadoComentario getEstado() {
        return estado;
    }

    public Instant getEditadoAt() {
        return editadoAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
