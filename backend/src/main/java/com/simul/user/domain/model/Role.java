package com.simul.user.domain.model;

/**
 * 사용자 역할 (DB·JWT에 접두사 없이 저장)
 * Spring Security에서는 hasRole("ADMIN") 형태로 사용
 */
public enum Role {
    USER,
    ADMIN
}
