package com.simul.user.application.port.in;

import java.util.UUID;

/**
 * 팔로우 유즈케이스 (Input Port)
 * - 타인을 팔로우하는 기능
 */
public interface FollowUserUseCase {

    /**
     * 팔로우
     * @param followerId 팔로우 하는 사용자 ID
     * @param followingId 팔로우 당하는 사용자 ID
     */
    void follow(UUID followerId, UUID followingId);
}
