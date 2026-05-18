package com.simul.auth.application.port.out;

import com.simul.auth.domain.model.PasswordResetCode;
import java.util.Optional;

public interface PasswordResetCodePort {
    void save(PasswordResetCode code);
    Optional<PasswordResetCode> findByEmail(String email);
    Optional<PasswordResetCode> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
}
