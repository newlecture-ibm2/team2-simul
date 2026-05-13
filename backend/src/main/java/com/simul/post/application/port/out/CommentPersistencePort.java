package com.simul.post.application.port.out;

import com.simul.post.domain.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentPersistencePort {
    Comment save(Comment comment);
    Optional<Comment> findById(UUID commentId);
    Page<Comment> findParentCommentsByPostId(UUID postId, Pageable pageable);
    List<Comment> findRepliesByParentId(UUID parentCommentId);
    List<Comment> findRepliesByParentIds(List<UUID> parentIds);
    boolean hasNonDeletedReplies(UUID parentCommentId);
    int countByPostId(UUID postId);
    int countActiveByPostId(UUID postId);
}
