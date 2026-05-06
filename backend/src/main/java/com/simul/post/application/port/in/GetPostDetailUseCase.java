package com.simul.post.application.port.in;

import com.simul.post.application.dto.PostDetailResponse;

import java.util.UUID;

public interface GetPostDetailUseCase {
    PostDetailResponse getPostDetail(UUID postId, UUID currentUserId);
}
