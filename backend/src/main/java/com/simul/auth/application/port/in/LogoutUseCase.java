package com.simul.auth.application.port.in;

/**
 * 로그아웃 유즈케이스 (Input Port)
 * - 리프레시 토큰을 Redis에서 삭제하여 강제 로그아웃 처리
 */
public interface LogoutUseCase {

    /**
     * 로그아웃 처리 (리프레시 토큰 무효화)
     *
     * @param refreshToken 무효화할 리프레시 토큰
     */
    void logout(String refreshToken);
}
