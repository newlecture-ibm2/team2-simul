package com.simul.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User JPA Repository
 * - Spring Data JPA가 인터페이스 기반으로 구현체 자동 생성
 * - DB 연결 시(DataSource 설정 완료 후) 자동으로 활성화됨
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * 소셜 로그인 제공자 + 제공자ID로 사용자 조회
     * WHERE provider = ? AND provider_id = ? AND deleted_at IS NULL
     */
    Optional<UserJpaEntity> findByProviderAndProviderIdAndDeletedAtIsNull(
        String provider, String providerId
    );

    /**
     * 탈퇴 여부와 상관없이 소셜 로그인 정보로 사용자 조회
     */
    Optional<UserJpaEntity> findByProviderAndProviderId(String provider, String providerId);

    /**
     * ID로 활성 사용자 조회 (Soft Delete 필터)
     */
    Optional<UserJpaEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

    /**
     * ID 목록으로 활성 사용자 조회
     */
    List<UserJpaEntity> findByUserIdInAndDeletedAtIsNull(List<UUID> userIds);

    /**
     * 인증되지 않은 오래된 가계정 삭제 (배치용)
     */
    int deleteByIsActiveFalseAndCreatedAtBefore(java.time.LocalDateTime cutoffDate);
}
