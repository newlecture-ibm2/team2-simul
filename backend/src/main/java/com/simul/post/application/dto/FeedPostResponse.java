package com.simul.post.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedPostResponse(
    UUID postId,
    UUID userId,
    String nickname,
    String profileImageUrl,
    String imageUrl,
    List<String> tags,
    String caption,
    int likeCount,
    boolean isLiked,
    LocalDateTime createdAt
) {}
