package co.edu.atlas.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria_terapeutica")
public class CategoriaTerapeutica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false)
    private int orden;

    protected CategoriaTerapeutica() {}

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getOrden() {
        return orden;
    }
}
