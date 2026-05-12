package com.simul.post.application.port.in;

import com.simul.post.application.dto.LikeUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetPostLikesUseCase {
    Page<LikeUserResponse> getPostLikes(UUID postId, Pageable pageable);
}
