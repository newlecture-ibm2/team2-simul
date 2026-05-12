package com.simul.post.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
    UUID commentId,
    UUID userId,
    String nickname,
    String profileImageUrl,
    String content,
    int depth,
    LocalDateTime createdAt,
    boolean isDeleted,
    List<CommentResponse> replies
) {}
