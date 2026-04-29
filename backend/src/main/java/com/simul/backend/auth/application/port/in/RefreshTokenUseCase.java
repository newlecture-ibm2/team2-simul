package com.simul.backend.auth.application.port.in;

import com.simul.backend.auth.application.dto.TokenResponse;

/**
 * 토큰 갱신 유즈케이스 (Input Port)
 */
public interface RefreshTokenUseCase {

    /**
     * Refresh Token으로 새로운 Access Token 발급
     *
     * @param refreshToken 기존 Refresh Token
     * @return 새로운 JWT 토큰
     * @throws BusinessException 토큰이 만료되었거나 유효하지 않은 경우
     */
    TokenResponse refreshToken(String refreshToken);
}
