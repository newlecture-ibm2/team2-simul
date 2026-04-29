package com.simul.backend.user.adapter.out.persistence;

import com.simul.backend.user.application.port.out.UserPersistencePort;
import com.simul.backend.user.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * User Persistence Adapter (Output Port 구현체)
 * - UserPersistencePort 인터페이스를 구현
 * - 내부적으로 Spring Data JPA Repository를 사용
 * - Service는 이 Adapter를 직접 알지 못함 (Port를 통해서만 접근)
 */
@Component
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        // Soft Delete된 사용자는 조회 제외
        return userJpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userJpaRepository.findByProviderAndProviderIdAndDeletedAtIsNull(
            provider, providerId
        );
    }
}
