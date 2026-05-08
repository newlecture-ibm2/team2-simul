package com.simul.auth.application.port.out;

import com.simul.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * 리프레시 토큰 영속성 포트 (Output Port)
 *
 * - Auth 서비스가 토큰을 저장/조회/삭제할 때 사용하는 인터페이스
 * - 실제 구현은 Redis Adapter에서 담당 (헥사고날 원칙)
 * - 서비스 레이어는 이 인터페이스만 알고, Redis의 존재를 모름
 */
public interface RefreshTokenPort {

    /**
     * 리프레시 토큰 저장
     * - 기존 토큰이 있으면 덮어씌움 (토큰 갱신 시)
     *
     * @param refreshToken 저장할 리프레시 토큰 도메인 객체
     */
    void save(RefreshToken refreshToken);

    /**
     * 토큰 문자열로 리프레시 토큰 조회
     *
     * @param token 리프레시 토큰 문자열
     * @return 존재하면 RefreshToken, 만료/삭제 시 Optional.empty()
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 특정 토큰 삭제 (로그아웃 시 사용)
     *
     * @param token 삭제할 리프레시 토큰 문자열
     */
    void deleteByToken(String token);

    /**
     * 특정 사용자의 모든 리프레시 토큰 삭제
     * - 비밀번호 변경, 계정 정지 등 모든 세션을 끊어야 할 때 사용
     *
     * @param userId 사용자 ID
     */
    void deleteAllByUserId(UUID userId);
}
