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
    
    Page<Post> findAllPublicPosts(java.time.LocalDateTime since, Pageable pageable);
    Page<Post> findFollowingPosts(List<UUID> userIds, java.time.LocalDateTime since, Pageable pageable);
    
    long countByUserId(UUID userId);
    long countProfilePostsByUserId(UUID userId);
    Page<Post> findByUserId(UUID userId, Pageable pageable);
    Page<Post> findProfilePostsByUserId(UUID userId, Pageable pageable);
    Page<Post> findPublicPostsByUserId(UUID userId, Pageable pageable);

    long countLikedPosts(UUID userId);
    Page<Post> findLikedPostsByUserId(UUID userId, Pageable pageable);

    Page<Post> findByCaption(String caption, Pageable pageable);
    Page<Post> findByTagName(String tagName, Pageable pageable);
    Page<Post> findByTagNameOrCaption(String tagQuery, String captionQuery, Pageable pageable);
}
