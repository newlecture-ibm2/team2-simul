package com.simul.auth.adapter.out.persistence;

import com.simul.auth.application.port.out.PasswordResetCodePort;
import com.simul.auth.domain.model.PasswordResetCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetCodePersistenceAdapter implements PasswordResetCodePort {

    private final PasswordResetCodeJpaRepository jpaRepository;

    @Override
    public void save(PasswordResetCode code) {
        jpaRepository.save(PasswordResetCodeJpaEntity.builder()
                .email(code.getEmail())
                .code(code.getCode())
                .expiryDate(code.getExpiryDate())
                .isVerified(code.isVerified())
                .build());
    }

    @Override
    public Optional<PasswordResetCode> findByEmail(String email) {
        return jpaRepository.findById(email)
                .map(entity -> PasswordResetCode.builder()
                        .email(entity.getEmail())
                        .code(entity.getCode())
                        .expiryDate(entity.getExpiryDate())
                        .isVerified(entity.isVerified())
                        .build());
    }

    @Override
    public Optional<PasswordResetCode> findByEmailAndCode(String email, String code) {
        return jpaRepository.findByEmailAndCode(email, code)
                .map(entity -> PasswordResetCode.builder()
                        .email(entity.getEmail())
                        .code(entity.getCode())
                        .expiryDate(entity.getExpiryDate())
                        .isVerified(entity.isVerified())
                        .build());
    }

    @Override
    public void deleteByEmail(String email) {
        jpaRepository.deleteById(email);
    }
}
