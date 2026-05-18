package com.simul.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class PasswordResetCode {
    private final String email;
    private final String code;
    private final LocalDateTime expiryDate;
    private boolean isVerified;

    public static PasswordResetCode create(String email, String code) {
        return PasswordResetCode.builder()
                .email(email)
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // 6자리 인증번호는 15분 동안 유효
                .isVerified(false)
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void verify() {
        this.isVerified = true;
    }
}
