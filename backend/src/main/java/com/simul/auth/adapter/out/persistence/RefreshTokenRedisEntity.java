package com.simul.auth.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

/**
 * Redis에 저장되는 리프레시 토큰 엔티티
 *
 * - @RedisHash: Redis에 "refreshToken:{token}" 형태의 키로 저장
 * - @TimeToLive: 설정된 초(seconds)가 지나면 Redis가 자동으로 삭제
 * - @Indexed: userId로도 검색할 수 있도록 보조 인덱스 생성
 */
@RedisHash("refreshToken")
public class RefreshTokenRedisEntity {

    /** 리프레시 토큰 문자열 (Redis Hash의 Key) */
    @Id
    private String token;

    /** 토큰 소유자 (보조 인덱스로 "사용자별 전체 삭제"에 활용) */
    @Indexed
    private String userId;

    /** 자동 만료 시간 (초 단위, Redis TTL) */
    @TimeToLive
    private long timeToLive;

    protected RefreshTokenRedisEntity() {}

    public RefreshTokenRedisEntity(String token, UUID userId, long timeToLive) {
        this.token = token;
        this.userId = userId.toString();
        this.timeToLive = timeToLive;
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public long getTimeToLive() { return timeToLive; }
}
