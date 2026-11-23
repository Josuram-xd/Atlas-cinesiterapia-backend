package co.edu.atlas.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "region_anatomica")
public class RegionAnatomica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Vista vista;

    @Column(name = "svg_id", nullable = false, unique = true, length = 60)
    private String svgId;

    @Column(nullable = false)
    private int orden;

    protected RegionAnatomica() {}

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Vista getVista() {
        return vista;
    }

    public String getSvgId() {
        return svgId;
    }

    public int getOrden() {
        return orden;
    }
}
