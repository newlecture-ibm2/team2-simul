package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PostJpaRepository extends JpaRepository<Post, UUID> {
    Page<Post> findAllByIsPublicTrueAndIsBlindedFalse(Pageable pageable);
    Page<Post> findAllByUserIdInAndIsPublicTrueAndIsBlindedFalse(List<UUID> userIds, Pageable pageable);
    
    long countByUserId(UUID userId);
    Page<Post> findAllByUserId(UUID userId, Pageable pageable);
    Page<Post> findAllByUserIdAndIsPublicTrue(UUID userId, Pageable pageable);
}
