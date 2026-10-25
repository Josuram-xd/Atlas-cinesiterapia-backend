package co.edu.atlas.metricas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

/** Sesion de uso: de {@code inicioAt} al ultimo ping. Su duracion es lo que mide el tiempo promedio de uso. */
@Entity
@Table(name = "sesion_uso")
public class SesionUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "inicio_at", nullable = false)
    private Instant inicioAt;

    @Column(name = "ultimo_ping_at", nullable = false)
    private Instant ultimoPingAt;

    @Column(name = "duracion_segundos", nullable = false)
    private long duracionSegundos;

    protected SesionUso() {}

    public SesionUso(Long usuarioId, Instant ahora) {
        this.usuarioId = usuarioId;
        this.inicioAt = ahora;
        this.ultimoPingAt = ahora;
    }

    /** Un ping despues de {@code inactividad} sin actividad ya no pertenece a esta sesion. */
    public boolean vigente(Instant ahora, Duration inactividad) {
        return !ahora.isAfter(ultimoPingAt.plus(inactividad));
    }

    public void registrarPing(Instant ahora) {
        this.ultimoPingAt = ahora;
        this.duracionSegundos = Duration.between(inicioAt, ahora).getSeconds();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Instant getInicioAt() {
        return inicioAt;
    }

    public Instant getUltimoPingAt() {
        return ultimoPingAt;
    }

    public long getDuracionSegundos() {
        return duracionSegundos;
    }
}
