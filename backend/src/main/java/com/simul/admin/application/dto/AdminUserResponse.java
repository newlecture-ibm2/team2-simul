package com.simul.admin.application.dto;

import com.simul.user.domain.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
    UUID userId,
    String nickname,
    String providerId,
    String role,
    String provider,
    boolean isActive,
    LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
            user.getUserId(),
            user.getNickname(),
            user.getProviderId(),
            user.getRole().name(),
            user.getProvider(),
            user.isActive(),
            user.getCreatedAt()
        );
    }
}
