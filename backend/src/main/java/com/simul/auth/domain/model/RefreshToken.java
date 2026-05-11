package com.simul.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 리프레시 토큰 도메인 모델 (순수 Java 객체)
 *
 * - Redis에 저장되는 리프레시 토큰의 비즈니스 표현
 * - JPA/Redis 등 인프라 종속성 없음 (헥사고날 원칙)
 * - TTL(timeToLive)은 초 단위로 자동 만료 시간을 나타냄
 */
@Getter
@Builder
public class RefreshToken {

    /** 리프레시 토큰 문자열 (Redis Key로 사용) */
    private String token;

    /** 토큰 소유자의 사용자 ID */
    private UUID userId;

    /** 자동 만료 시간 (초 단위, 예: 14일 = 1209600초) */
    private long timeToLive;
}
