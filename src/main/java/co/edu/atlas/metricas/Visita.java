package co.edu.atlas.metricas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Visita registrada con fecha y usuario (RF_032). {@code fecha} es el dia UTC de {@code createdAt}. */
@Entity
@Table(name = "visita")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVisita tipo;

    @Column(name = "ejercicio_id")
    private Long ejercicioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Visita() {}

    public Visita(Long usuarioId, TipoVisita tipo, Long ejercicioId, Instant ahora) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.ejercicioId = ejercicioId;
        this.createdAt = ahora;
        this.fecha = ahora.atOffset(ZoneOffset.UTC).toLocalDate();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public TipoVisita getTipo() {
        return tipo;
    }

    public Long getEjercicioId() {
        return ejercicioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
