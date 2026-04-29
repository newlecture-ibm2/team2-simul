package com.simul.backend.auth.application.port.in;

import com.simul.backend.auth.application.dto.SocialLoginCommand;
import com.simul.backend.auth.application.dto.TokenResponse;

/**
 * 소셜 로그인 유즈케이스 (Input Port)
 * - Controller에서 이 인터페이스를 호출
 * - 실제 구현은 AuthService
 */
public interface SocialLoginUseCase {

    /**
     * 소셜 로그인 처리
     * 1. OAuth2 제공자에서 사용자 정보 조회
     * 2. 기존 회원이면 로그인, 신규면 자동 가입
     * 3. JWT Access + Refresh Token 발급
     *
     * @param command 소셜 로그인 요청 (provider, code, redirectUri)
     * @return JWT 토큰 + 신규 가입 여부
     */
    TokenResponse socialLogin(SocialLoginCommand command);
}
