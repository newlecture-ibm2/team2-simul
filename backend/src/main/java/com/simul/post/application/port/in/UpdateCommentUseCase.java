package com.simul.post.application.port.in;

import com.simul.post.application.dto.CommentResponse;
import java.util.UUID;

public interface UpdateCommentUseCase {
    CommentResponse updateComment(UUID commentId, UUID userId, String content);
}
