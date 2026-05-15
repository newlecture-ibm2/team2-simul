package com.simul.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, String> {
    Optional<EmailVerificationJpaEntity> findByToken(String token);
    
    int deleteByExpiryDateBefore(java.time.LocalDateTime cutoffDate);
}
