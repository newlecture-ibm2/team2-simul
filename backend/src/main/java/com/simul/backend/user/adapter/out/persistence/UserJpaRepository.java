package com.simul.backend.user.adapter.out.persistence;

import com.simul.backend.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * User JPA Repository
 * - Spring Data JPA가 인터페이스 기반으로 구현체 자동 생성
 * - DB 연결 시(DataSource 설정 완료 후) 자동으로 활성화됨
 */
public interface UserJpaRepository extends JpaRepository<User, UUID> {

    /**
     * 소셜 로그인 제공자 + 제공자ID로 사용자 조회
     * WHERE provider = ? AND provider_id = ? AND deleted_at IS NULL
     */
    Optional<User> findByProviderAndProviderIdAndDeletedAtIsNull(
        String provider, String providerId
    );

    /**
     * ID로 활성 사용자 조회 (Soft Delete 필터)
     */
    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);
}
