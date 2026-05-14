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
        java.util.Map<String, List<String>> imageTagsMap,
        List<String> manualTags,
        String caption,
        int likeCount,
        int viewCount,
        int commentCount,
        boolean isLiked,
        boolean isPublic,
        int reportCount,
        boolean isWarned,
        LocalDateTime createdAt
) {
}
