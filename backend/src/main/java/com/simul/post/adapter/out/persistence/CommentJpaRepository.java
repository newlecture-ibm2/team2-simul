package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentJpaRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByPostIdAndParentCommentIdIsNullOrderByCreatedAtAsc(UUID postId, Pageable pageable);
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
    List<Comment> findByParentCommentIdInOrderByCreatedAtAsc(List<UUID> parentIds);

    boolean existsByParentCommentIdAndDeletedAtIsNull(UUID parentCommentId);

    int countByPostId(UUID postId);
    int countByPostIdAndDeletedAtIsNull(UUID postId);
}
