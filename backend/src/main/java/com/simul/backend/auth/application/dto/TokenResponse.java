package com.simul.backend.auth.application.dto;

/**
 * JWT 토큰 응답 DTO
 */
public record TokenResponse(
    String accessToken,
    String refreshToken,
    boolean isNewUser   // true면 최초 가입 사용자 (온보딩 등 분기용)
) {}
