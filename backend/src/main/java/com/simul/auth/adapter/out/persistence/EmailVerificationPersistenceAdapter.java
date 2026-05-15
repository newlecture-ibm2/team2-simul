package com.simul.auth.adapter.out.persistence;

import com.simul.auth.application.port.out.EmailVerificationPort;
import com.simul.auth.domain.model.EmailVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationPersistenceAdapter implements EmailVerificationPort {

    private final EmailVerificationJpaRepository repository;

    @Override
    public void save(EmailVerification verification) {
        EmailVerificationJpaEntity entity = EmailVerificationJpaEntity.builder()
                .email(verification.getEmail())
                .token(verification.getToken())
                .expiryDate(verification.getExpiryDate())
                .isVerified(verification.isVerified())
                .build();
        repository.save(entity);
    }

    @Override
    public Optional<EmailVerification> findByToken(String token) {
        return repository.findByToken(token)
                .map(entity -> EmailVerification.builder()
                        .email(entity.getEmail())
                        .token(entity.getToken())
                        .expiryDate(entity.getExpiryDate())
                        .isVerified(entity.isVerified())
                        .build());
    }

    @Override
    public void deleteByEmail(String email) {
        repository.deleteById(email);
    }
}
