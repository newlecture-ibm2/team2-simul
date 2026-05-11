package com.simul.auth.adapter.out.persistence;

import com.simul.auth.application.port.out.AccessTokenBlacklistPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Access Token 블랙리스트 Redis 어댑터
 *
 * - 로그아웃된 Access Token을 Redis에 "blacklist:{token}" 키로 저장
 * - TTL을 토큰의 남은 유효기간으로 설정하여 만료 시 자동 삭제
 * - JwtAuthenticationFilter에서 매 요청마다 블랙리스트 여부를 체크
 */
@Component
public class AccessTokenBlacklistRedisAdapter implements AccessTokenBlacklistPort {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public AccessTokenBlacklistRedisAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToBlacklist(String token, long expirySeconds) {
        if (expirySeconds > 0) {
            redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "logout",
                expirySeconds,
                TimeUnit.SECONDS
            );
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
