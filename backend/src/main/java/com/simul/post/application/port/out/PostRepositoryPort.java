package com.simul.post.application.port.out;

import com.simul.post.domain.model.Post;
import java.util.Optional;
import java.util.UUID;

public interface PostRepositoryPort {
    Post save(Post post);
    Optional<Post> findById(UUID postId);
    void deleteById(UUID postId);
}
