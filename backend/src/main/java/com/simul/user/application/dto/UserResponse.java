package com.simul.user.application.dto;

import com.simul.user.domain.model.User;
import java.util.UUID;

/**
 * 사용자 정보 응답 DTO
 * - 엔티티를 직접 노출하지 않고 필요한 정보만 전달
 */
public record UserResponse(
    UUID userId,
    String nickname,
    String name,
    String gender,
    String bio,
    String profileImageUrl,
    String bannerImageUrl,
    boolean isPublic,
    String role
) {
    /**
     * User 엔티티 → UserResponse 변환
     */
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getUserId(),
            user.getNickname(),
            user.getName(),
            user.getGender().name(),
            user.getBio(),
            user.getProfileImageUrl(),
            user.getBannerImageUrl(),
            user.isPublic(),
            user.getRole().name()
        );
    }
}
