package com.simul.post.application.port.in;

import com.simul.post.application.dto.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LoadCommentUseCase {
    Page<CommentResponse> loadComments(UUID postId, Pageable pageable);
}
