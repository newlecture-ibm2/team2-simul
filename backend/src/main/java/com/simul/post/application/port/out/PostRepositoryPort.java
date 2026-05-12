package com.simul.post.application.port.out;

import com.simul.post.domain.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepositoryPort {
    Post save(Post post);
    Optional<Post> findById(UUID postId);
    void deleteById(UUID postId);
    
    Page<Post> findAllPublicPosts(Pageable pageable);
    Page<Post> findFollowingPosts(List<UUID> userIds, Pageable pageable);
    
    long countByUserId(UUID userId);
    Page<Post> findByUserId(UUID userId, Pageable pageable);
    Page<Post> findPublicPostsByUserId(UUID userId, Pageable pageable);
}
