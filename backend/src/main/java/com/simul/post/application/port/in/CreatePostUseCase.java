package com.simul.post.application.port.in;

import com.simul.post.application.dto.CreatePostCommand;
import com.simul.post.domain.model.Post;

public interface CreatePostUseCase {
    Post createPost(CreatePostCommand command);
}
