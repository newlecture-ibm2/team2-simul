package com.simul.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이메일 인증 도메인 모델
 */
@Getter
@Builder
public class EmailVerification {
    private String email;
    private String token;
    private LocalDateTime expiryDate;
    private boolean isVerified;

    /**
     * 새로운 인증 정보 생성 (만료 시간 24시간)
     */
    public static EmailVerification create(String email) {
        return EmailVerification.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .isVerified(false)
                .build();
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 인증 완료 처리
     */
    public void verify() {
        this.isVerified = true;
    }
}
