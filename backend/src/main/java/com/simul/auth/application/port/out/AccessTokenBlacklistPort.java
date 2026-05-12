package com.simul.auth.application.port.out;

/**
 * Access Token 블랙리스트 포트 (Output Port)
 *
 * - 로그아웃 시 아직 만료되지 않은 Access Token을 블랙리스트에 등록
 * - JWT 필터에서 블랙리스트 여부를 체크하여 무효화된 토큰 차단
 * - 실제 구현은 Redis Adapter에서 담당
 */
public interface AccessTokenBlacklistPort {

    /**
     * Access Token을 블랙리스트에 등록
     *
     * @param token      블랙리스트에 등록할 Access Token
     * @param expirySeconds 남은 만료 시간 (초 단위, Redis TTL로 사용)
     */
    void addToBlacklist(String token, long expirySeconds);

    /**
     * 해당 Access Token이 블랙리스트에 존재하는지 확인
     *
     * @param token 확인할 Access Token
     * @return 블랙리스트에 존재하면 true
     */
    boolean isBlacklisted(String token);
}
