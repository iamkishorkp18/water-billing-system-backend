package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}