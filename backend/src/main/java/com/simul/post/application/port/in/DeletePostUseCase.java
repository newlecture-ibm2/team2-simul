package com.simul.post.application.port.in;

import java.util.UUID;

public interface DeletePostUseCase {
    void deletePost(UUID postId, UUID currentUserId);
}
