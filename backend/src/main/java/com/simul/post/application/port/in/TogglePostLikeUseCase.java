package com.simul.post.application.port.in;

import com.simul.post.application.dto.ToggleLikeResponse;

import java.util.UUID;

/**
 * [Hexagonal - Input Port]
 * 좋아요 토글 유스케이스
 */
public interface TogglePostLikeUseCase {

    /**
     * 좋아요 토글 (like ↔ unlike)
     * @param postId 대상 게시물 ID
     * @param userId 요청 유저 ID
     * @return 토글 후 상태 (isLiked, likeCount)
     */
    ToggleLikeResponse toggleLike(UUID postId, UUID userId);
}
