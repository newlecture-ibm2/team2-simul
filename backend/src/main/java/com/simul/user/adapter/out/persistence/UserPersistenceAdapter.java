package com.simul.user.adapter.out.persistence;

import com.simul.user.application.port.out.UserPersistencePort;
import com.simul.user.domain.model.User;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Persistence Adapter (Output Port 구현체)
 * - UserPersistencePort 인터페이스를 구현
 * - 내부적으로 Spring Data JPA Repository를 사용
 * - Service는 이 Adapter를 직접 알지 못함 (Port를 통해서만 접근)
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = userPersistenceMapper.mapToJpaEntity(user);
        
        // Handle soft delete updates if needed
        if (!user.isActive()) {
            entity.softDelete();
        }
        
        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        return userPersistenceMapper.mapToDomainEntity(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userJpaRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(userPersistenceMapper::mapToDomainEntity);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userJpaRepository.findByProviderAndProviderIdAndDeletedAtIsNull(provider, providerId)
                .map(userPersistenceMapper::mapToDomainEntity);
    }

    @Override
    public List<User> findByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userJpaRepository.findByUserIdInAndDeletedAtIsNull(userIds).stream()
                .map(userPersistenceMapper::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<User> loadAllUsers(org.springframework.data.domain.Pageable pageable) {
        return userJpaRepository.findAllByDeletedAtIsNull(pageable)
                .map(userPersistenceMapper::mapToDomainEntity);
    }
}
