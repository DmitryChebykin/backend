package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.region_stat.domain.entity.passwordReset.PasswordResetToken;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
}