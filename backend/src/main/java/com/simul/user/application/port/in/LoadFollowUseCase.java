package com.simul.user.application.port.in;

import com.simul.user.application.dto.FollowCountResponse;
import java.util.UUID;

/**
 * 팔로우 조회 유즈케이스 (Input Port)
 * - 팔로워/팔로잉 수 조회 및 팔로우 여부 확인
 */
public interface LoadFollowUseCase {

    /**
     * 특정 사용자의 팔로워/팔로잉 수 조회
     */
    FollowCountResponse getFollowCounts(UUID userId);

    /**
     * 팔로우 여부 확인
     * @param followerId 팔로우 하는 사용자 ID
     * @param followingId 팔로우 당하는 사용자 ID
     * @return 팔로우 중이면 true
     */
    boolean isFollowing(UUID followerId, UUID followingId);

    /**
     * 특정 사용자의 팔로워 목록 조회
     * @param currentUserId 요청한 사용자 (팔로우 여부 확인용, 비로그인시 null)
     * @param targetUserId 조회할 대상 사용자
     */
    java.util.List<com.simul.user.application.dto.FollowUserResponse> getFollowers(UUID currentUserId, UUID targetUserId);

    /**
     * 특정 사용자의 팔로잉 목록 조회
     * @param currentUserId 요청한 사용자 (팔로우 여부 확인용, 비로그인시 null)
     * @param targetUserId 조회할 대상 사용자
     */
    java.util.List<com.simul.user.application.dto.FollowUserResponse> getFollowings(UUID currentUserId, UUID targetUserId);

    /**
     * 특정 사용자의 팔로워 ID 목록만 조회 (알림 발송용)
     * @param userId 대상 사용자 ID
     * @return 팔로워 UUID 목록
     */
    java.util.List<UUID> getFollowerIds(UUID userId);

    /**
     * 특정 사용자의 팔로잉 ID 목록만 조회 (피드 조회용)
     * @param userId 대상 사용자 ID
     * @return 팔로잉 UUID 목록
     */
    java.util.List<UUID> getFollowingIds(UUID userId);
}
