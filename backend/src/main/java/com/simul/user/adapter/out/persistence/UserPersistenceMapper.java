package com.simul.user.adapter.out.persistence;

import com.simul.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity mapToJpaEntity(User user) {
        return UserJpaEntity.builder()
                .userId(user.getUserId())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .name(user.getName())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .isPublic(user.isPublic())
                .role(user.getRole())
                .isActive(user.isActive())
                .build();
    }

    public User mapToDomainEntity(UserJpaEntity jpaEntity) {
        return User.builder()
                .userId(jpaEntity.getUserId())
                .provider(jpaEntity.getProvider())
                .providerId(jpaEntity.getProviderId())
                .name(jpaEntity.getName())
                .nickname(jpaEntity.getNickname())
                .gender(jpaEntity.getGender())
                .bio(jpaEntity.getBio())
                .profileImageUrl(jpaEntity.getProfileImageUrl())
                .isPublic(jpaEntity.isPublic())
                .role(jpaEntity.getRole())
                .isActive(jpaEntity.isActive())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .deletedAt(jpaEntity.getDeletedAt())
                .build();
    }
}
