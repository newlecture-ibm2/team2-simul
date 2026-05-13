package com.simul.post.application.port.in;

import com.simul.post.application.dto.CommentResponse;
import com.simul.post.application.dto.CreateCommentCommand;

import java.util.UUID;

public interface CreateCommentUseCase {
    CommentResponse createComment(UUID postId, UUID userId, CreateCommentCommand command);
}
