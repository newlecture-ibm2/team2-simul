package com.simul.backend.auth.application.dto;

/**
 * 소셜 로그인 요청 커맨드
 * - 프론트엔드에서 받은 OAuth2 인가 코드와 제공자 정보
 */
public record SocialLoginCommand(
    String provider,      // kakao, naver, google
    String code,          // OAuth2 인가 코드
    String redirectUri    // 프론트엔드 콜백 URL (OAuth2 규약상 필요)
) {}
