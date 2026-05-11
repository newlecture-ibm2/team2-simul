package com.simul.user.application.dto;

import java.util.UUID;

public record FollowUserResponse(
    UUID userId,
    String nickname,
    String profileImageUrl,
    boolean isFollowing
) {}
