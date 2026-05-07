package com.simul.post.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetailResponse(
        UUID postId,
        UUID userId,
        String nickname,
        String profileImageUrl,
        List<String> images,
        List<String> tags,
        String caption,
        int likeCount,
        int viewCount,
        boolean isLiked,
        LocalDateTime createdAt
) {
}
