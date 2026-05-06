package com.simul.post.application.dto;

/**
 * 좋아요 토글 응답 DTO
 */
public record ToggleLikeResponse(
    boolean isLiked,
    int likeCount
) {}
