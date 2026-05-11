package com.simul.user.adapter.out.persistence;

import com.simul.user.domain.model.Follow;
import org.springframework.stereotype.Component;

/**
 * Follow 도메인 모델 ↔ JPA 엔티티 변환 매퍼
 */
@Component
public class FollowPersistenceMapper {

    public FollowJpaEntity mapToJpaEntity(Follow follow) {
        return FollowJpaEntity.builder()
                .followId(follow.getFollowId())
                .followerId(follow.getFollowerId())
                .followingId(follow.getFollowingId())
                .build();
    }

    public Follow mapToDomainEntity(FollowJpaEntity jpaEntity) {
        return Follow.builder()
                .followId(jpaEntity.getFollowId())
                .followerId(jpaEntity.getFollowerId())
                .followingId(jpaEntity.getFollowingId())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }
}
