package com.simul.post.application.port.in;

import java.util.UUID;

public interface ReportPostUseCase {
    void reportPost(UUID postId, UUID reporterId, String reason);
}
