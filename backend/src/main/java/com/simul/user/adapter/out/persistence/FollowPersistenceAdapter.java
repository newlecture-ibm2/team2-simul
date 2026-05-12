package com.simul.user.adapter.out.persistence;

import com.simul.user.application.port.out.FollowPersistencePort;
import com.simul.user.domain.model.Follow;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Follow Persistence Adapter (Output Port 구현체)
 * - FollowPersistencePort 인터페이스를 구현
 * - 내부적으로 Spring Data JPA Repository를 사용
 */
@Component
@RequiredArgsConstructor
public class FollowPersistenceAdapter implements FollowPersistencePort {

    private final FollowJpaRepository followJpaRepository;
    private final FollowPersistenceMapper followPersistenceMapper;

    @Override
    public Follow save(Follow follow) {
        FollowJpaEntity entity = followPersistenceMapper.mapToJpaEntity(follow);
        FollowJpaEntity savedEntity = followJpaRepository.save(entity);
        return followPersistenceMapper.mapToDomainEntity(savedEntity);
    }

    @Override
    public void delete(UUID followerId, UUID followingId) {
        followJpaRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public boolean exists(UUID followerId, UUID followingId) {
        return followJpaRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public long countFollowers(UUID userId) {
        return followJpaRepository.countByFollowingId(userId);
    }

    @Override
    public long countFollowings(UUID userId) {
        return followJpaRepository.countByFollowerId(userId);
    }

    @Override
    public java.util.List<UUID> getFollowerIds(UUID userId) {
        return followJpaRepository.findAllByFollowingId(userId).stream()
                .map(FollowJpaEntity::getFollowerId)
                .toList();
    }

    @Override
    public java.util.List<UUID> getFollowingIds(UUID userId) {
        return followJpaRepository.findAllByFollowerId(userId).stream()
                .map(FollowJpaEntity::getFollowingId)
                .toList();
    }
}
