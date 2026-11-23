package co.edu.atlas.auth.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_perfil", nullable = false)
    private RolPerfil rolPerfil;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_sistema", nullable = false)
    private RolSistema rolSistema = RolSistema.USER;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Usuario() {}

    public Usuario(String correo, String passwordHash, RolPerfil rolPerfil, RolSistema rolSistema) {
        this.correo = normalizar(correo);
        this.passwordHash = passwordHash;
        this.rolPerfil = rolPerfil;
        this.rolSistema = rolSistema;
    }

    public static String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
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

    public Long getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolPerfil getRolPerfil() {
        return rolPerfil;
    }

    public RolSistema getRolSistema() {
        return rolSistema;
    }

    public void setRolSistema(RolSistema rolSistema) {
        this.rolSistema = rolSistema;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
