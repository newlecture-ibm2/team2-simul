package com.simul.auth.adapter.out.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * 리프레시 토큰 Redis 레포지토리
 *
 * - Spring Data Redis의 CrudRepository를 상속
 * - 기본 제공: save(), findById(), deleteById(), existsById() 등
 * - @Indexed가 붙은 userId 필드로 커스텀 쿼리 자동 생성
 */
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenRedisEntity, String> {

    /**
     * 특정 사용자의 모든 리프레시 토큰 조회
     * (@Indexed 덕분에 자동으로 Redis 보조 인덱스를 활용)
     */
    List<RefreshTokenRedisEntity> findByUserId(String userId);
}
