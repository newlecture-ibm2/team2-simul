package com.simul.auth.application.port.in;

/**
 * 로그아웃 유즈케이스 (Input Port)
 * - 리프레시 토큰을 Redis에서 삭제하여 강제 로그아웃 처리
 */
public interface LogoutUseCase {

    /**
     * 로그아웃 처리
     * - 리프레시 토큰을 Redis에서 삭제하여 재발급 차단
     * - Access Token을 블랙리스트에 등록하여 남은 유효기간 동안 사용 차단
     *
     * @param refreshToken 무효화할 리프레시 토큰 (nullable)
     * @param accessToken  블랙리스트에 등록할 액세스 토큰 (nullable)
     */
    void logout(String refreshToken, String accessToken);
}
