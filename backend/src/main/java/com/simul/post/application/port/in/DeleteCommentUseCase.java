package com.simul.post.application.port.in;

import java.util.UUID;

public interface DeleteCommentUseCase {
    void deleteComment(UUID commentId, UUID userId);
}
