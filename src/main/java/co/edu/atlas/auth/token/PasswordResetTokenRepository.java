package co.edu.atlas.auth.token;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PasswordResetToken t set t.usedAt = :ahora where t.usuarioId = :usuarioId and t.usedAt is null")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);
}
