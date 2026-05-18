package com.simul.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeJpaEntity, String> {
    Optional<PasswordResetCodeJpaEntity> findByEmailAndCode(String email, String code);
}
