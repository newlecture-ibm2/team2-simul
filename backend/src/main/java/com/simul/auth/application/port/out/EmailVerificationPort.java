package com.simul.auth.application.port.out;

import com.simul.auth.domain.model.EmailVerification;

import java.util.Optional;

public interface EmailVerificationPort {
    void save(EmailVerification verification);
    Optional<EmailVerification> findByToken(String token);
    void deleteByEmail(String email);
}
