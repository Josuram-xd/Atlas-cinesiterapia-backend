package co.edu.atlas.comentario.filtro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/** Entrada de la lista de palabras ofensivas; {@code palabraNormalizada} es la forma que compara el filtro. */
@Entity
@Table(name = "palabra_ofensiva")
public class PalabraOfensiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String palabra;

    @Column(name = "palabra_normalizada", nullable = false, unique = true, length = 60)
    private String palabraNormalizada;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PalabraOfensiva() {}

    public PalabraOfensiva(String palabra) {
        cambiar(palabra);
    }

    public void cambiar(String nueva) {
        this.palabra = nueva.strip();
        this.palabraNormalizada = NormalizadorOfensivo.normalizarPalabra(nueva);
    }

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getPalabra() {
        return palabra;
    }

    public String getPalabraNormalizada() {
        return palabraNormalizada;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
