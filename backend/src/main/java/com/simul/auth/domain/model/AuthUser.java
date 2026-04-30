package com.simul.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 인증(Auth) 도메인에서 다루는 사용자 모델
 * - users 테이블의 데이터 중 '인증/인가'에 필요한 핵심 데이터(ID, 역할, 상태)만 캡슐화합니다.
 * - 프로필 정보(이름, 닉네임, 성별 등)는 가지지 않습니다.
 */
@Getter
@Builder
public class AuthUser {
    
    private UUID userId;
    private AuthRole role;
    private boolean isActive;

    /**
     * 사용자가 현재 로그인 가능한 상태인지 검증합니다.
     */
    public boolean canLogin() {
        return this.isActive;
    }
}
