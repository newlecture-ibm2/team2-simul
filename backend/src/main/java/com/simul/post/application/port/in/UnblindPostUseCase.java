package com.simul.post.application.port.in;

import java.util.UUID;

public interface UnblindPostUseCase {
    void unblindPost(UUID postId);
}
