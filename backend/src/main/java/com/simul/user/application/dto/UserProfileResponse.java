package com.simul.user.application.dto;

import com.simul.user.domain.model.User;
import java.util.UUID;

/**
 * 사용자 프로필 응답 DTO (팔로우 정보 포함)
 * - 타인 프로필 조회 시 팔로워/팔로잉 수 및 팔로우 여부를 포함
 */
public record UserProfileResponse(
    UUID userId,
    String nickname,
    String name,
    String gender,
    String bio,
    String profileImageUrl,
    String bannerImageUrl,
    boolean isPublic,
    String role,
    long followerCount,
    long followingCount,
    long postCount,
    boolean isFollowing,
    String provider
) {
    /**
     * User 엔티티 + 팔로우 정보 + 게시물 수 → UserProfileResponse 변환
     */
    public static UserProfileResponse from(User user, long followerCount, long followingCount, long postCount, boolean isFollowing) {
        return new UserProfileResponse(
            user.getUserId(),
            user.getNickname(),
            user.getName(),
            user.getGender().name(),
            user.getBio(),
            user.getProfileImageUrl(),
            user.getBannerImageUrl(),
            user.isPublic(),
            user.getRole().name(),
            followerCount,
            followingCount,
            postCount,
            isFollowing,
            user.getProvider()
        );
    }
}
