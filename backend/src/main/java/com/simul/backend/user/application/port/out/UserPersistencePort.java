package com.simul.backend.user.application.port.out;

import com.simul.backend.user.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 저장소 포트 (Output Port)
 * - 실제 구현은 UserPersistenceAdapter가 담당
 * - Service 계층은 이 인터페이스에만 의존 (헥사고날 원칙)
 */
public interface UserPersistencePort {

    /**
     * 사용자 저장 (생성 및 수정)
     */
    User save(User user);

    /**
     * ID로 사용자 조회
     */
    Optional<User> findById(UUID userId);

    /**
     * 소셜 로그인 제공자 + 제공자ID로 사용자 조회
     * (기존 회원인지 확인용)
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
