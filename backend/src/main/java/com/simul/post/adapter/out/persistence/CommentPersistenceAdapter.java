package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.CommentPersistencePort;
import com.simul.post.domain.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentPersistencePort {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(UUID commentId) {
        return commentJpaRepository.findById(commentId);
    }

    @Override
    public Page<Comment> findParentCommentsByPostId(UUID postId, Pageable pageable) {
        return commentJpaRepository.findByPostIdAndParentCommentIdIsNullOrderByCreatedAtAsc(postId, pageable);
    }

    @Override
    public List<Comment> findRepliesByParentId(UUID parentCommentId) {
        return commentJpaRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId);
    }

    @Override
    public List<Comment> findRepliesByParentIds(List<UUID> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return List.of();
        }
        return commentJpaRepository.findByParentCommentIdInOrderByCreatedAtAsc(parentIds);
    }

    @Override
    public boolean hasNonDeletedReplies(UUID parentCommentId) {
        return commentJpaRepository.existsByParentCommentIdAndDeletedAtIsNull(parentCommentId);
    }

    @Override
    public int countByPostId(UUID postId) {
        return commentJpaRepository.countByPostId(postId);
    }

    @Override
    public int countActiveByPostId(UUID postId) {
        return commentJpaRepository.countByPostIdAndDeletedAtIsNull(postId);
    }
}
