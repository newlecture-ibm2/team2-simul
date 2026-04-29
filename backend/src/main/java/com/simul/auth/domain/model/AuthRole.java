package com.simul.auth.domain.model;

/**
 * 인증(Auth) 도메인에서 사용하는 역할(Role) 모델
 * - User 도메인의 Role과 별개로 인증/인가 로직에만 집중합니다.
 */
public enum AuthRole {
    USER,
    ADMIN;
    
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
