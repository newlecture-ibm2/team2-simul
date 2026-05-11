package com.simul.user.application.port.in;

import java.util.UUID;

/**
 * 언팔로우 유즈케이스 (Input Port)
 * - 팔로우 해제 기능
 */
public interface UnfollowUserUseCase {

    /**
     * 언팔로우
     * @param followerId 팔로우 해제하는 사용자 ID
     * @param followingId 팔로우 해제 대상 사용자 ID
     */
    void unfollow(UUID followerId, UUID followingId);
}
