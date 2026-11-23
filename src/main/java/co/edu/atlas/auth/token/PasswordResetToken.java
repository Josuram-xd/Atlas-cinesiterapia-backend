package co.edu.atlas.auth.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PasswordResetToken() {}

    public PasswordResetToken(Long usuarioId, String tokenHash, Instant expiresAt) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public boolean esUtilizable(Instant ahora) {
        return usedAt == null && expiresAt.isAfter(ahora);
    }

    public void marcarUsado(Instant ahora) {
        usedAt = ahora;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
