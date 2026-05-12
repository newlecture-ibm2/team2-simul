package com.simul.post.application.dto;

import java.util.UUID;

public record LikeUserResponse(
        UUID userId,
        String nickname,
        String profileImageUrl
) {}
