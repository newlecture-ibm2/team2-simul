package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface PostJpaRepository extends JpaRepository<Post, UUID> {
    Page<Post> findAllByIsPublicTrueAndIsBlindedFalse(Pageable pageable);
    Page<Post> findAllByIsPublicTrueAndIsBlindedFalseAndCreatedAtAfter(java.time.LocalDateTime createdAt, Pageable pageable);
    
    Page<Post> findAllByUserIdInAndIsPublicTrueAndIsBlindedFalse(List<UUID> userIds, Pageable pageable);
    Page<Post> findAllByUserIdInAndIsPublicTrueAndIsBlindedFalseAndCreatedAtAfter(List<UUID> userIds, java.time.LocalDateTime createdAt, Pageable pageable);

    
    long countByUserId(UUID userId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.userId = :userId AND p.deletedAt IS NULL AND (p.baseImageId IS NULL OR p.isPublic = true)")
    long countProfilePostsByUserId(UUID userId);

    Page<Post> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.deletedAt IS NULL AND (p.baseImageId IS NULL OR p.isPublic = true)")
    Page<Post> findProfilePostsByUserId(UUID userId, Pageable pageable);

    Page<Post> findAllByUserIdAndIsPublicTrue(UUID userId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN PostLike l ON l.postId = p.postId WHERE l.userId = :userId AND p.deletedAt IS NULL")
    Page<Post> findLikedPostsByUserId(UUID userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p JOIN PostLike l ON l.postId = p.postId WHERE l.userId = :userId AND p.deletedAt IS NULL")
    long countLikedPosts(UUID userId);

    Page<Post> findAllByCaptionContainingIgnoreCaseAndIsPublicTrueAndIsBlindedFalse(String caption, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN PostTag pt ON p.postId = pt.postId JOIN Tag t ON pt.tag.id = t.id WHERE t.name = :tagName AND p.isPublic = true AND p.isBlinded = false AND p.deletedAt IS NULL")
    Page<Post> findByTagName(@org.springframework.data.repository.query.Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN PostTag pt ON p.postId = pt.postId LEFT JOIN Tag t ON pt.tag.id = t.id WHERE (t.name = :tagQuery OR LOWER(p.caption) LIKE LOWER(CONCAT('%', :captionQuery, '%'))) AND p.isPublic = true AND p.isBlinded = false AND p.deletedAt IS NULL")
    Page<Post> findByTagNameOrCaption(@org.springframework.data.repository.query.Param("tagQuery") String tagQuery, @org.springframework.data.repository.query.Param("captionQuery") String captionQuery, Pageable pageable);
}
