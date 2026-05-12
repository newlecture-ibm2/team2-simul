package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.dto.CommentResponse;
import com.simul.post.application.dto.CreateCommentCommand;
import com.simul.post.application.port.in.CreateCommentUseCase;
import com.simul.post.application.port.in.DeleteCommentUseCase;
import com.simul.post.application.port.in.LoadCommentUseCase;
import com.simul.post.application.port.out.CommentPersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Comment;
import com.simul.user.application.port.in.LoadUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService implements LoadCommentUseCase, CreateCommentUseCase, DeleteCommentUseCase {

    private final CommentPersistencePort commentPersistencePort;
    private final PostRepositoryPort postPersistencePort;
    private final LoadUserUseCase loadUserUseCase;

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> loadComments(UUID postId, Pageable pageable) {
        // 1. Check if post exists
        postPersistencePort.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Load Parent Comments
        Page<Comment> parentComments = commentPersistencePort.findParentCommentsByPostId(postId, pageable);

        // 3. Load all replies for these parents in one go (or per parent if preferred, but let's optimize)
        // For simplicity and to maintain current structure, we'll keep the reply fetch but batch the users.
        
        // Collect all user IDs from parents and their replies
        java.util.Set<UUID> userIds = new java.util.HashSet<>();
        parentComments.forEach(c -> {
            userIds.add(c.getUserId());
            List<Comment> replies = commentPersistencePort.findRepliesByParentId(c.getCommentId());
            replies.forEach(r -> userIds.add(r.getUserId()));
        });

        // Batch load all users
        java.util.Map<UUID, com.simul.user.application.dto.UserResponse> userMap = 
            loadUserUseCase.loadUsers(new java.util.ArrayList<>(userIds));

        // 4. Map to DTO
        return parentComments.map(c -> {
            List<Comment> replies = commentPersistencePort.findRepliesByParentId(c.getCommentId());
            List<CommentResponse> replyDtos = replies.stream()
                    .map(r -> createCommentResponse(r, List.of(), userMap))
                    .collect(Collectors.toList());
            return createCommentResponse(c, replyDtos, userMap);
        });
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID postId, UUID userId, CreateCommentCommand command) {
        // 1. Verify Post
        postPersistencePort.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        int depth = 1;
        UUID parentCommentId = command.getParentCommentId();

        if (parentCommentId != null) {
            Comment parentComment = commentPersistencePort.findById(parentCommentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            
            // Only allow 2 depth
            if (parentComment.getDepth() >= 2) {
                parentCommentId = parentComment.getParentCommentId();
            }
            depth = 2;
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(parentCommentId)
                .depth(depth)
                .content(command.getContent())
                .build();

        Comment savedComment = commentPersistencePort.save(comment);

        // TODO: Notification creation will be added here

        return mapToResponse(savedComment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentPersistencePort.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.softDelete();
        commentPersistencePort.save(comment);
    }


    private CommentResponse mapToResponse(Comment comment) {
        var userProfile = loadUserUseCase.loadUser(comment.getUserId());
        java.util.Map<UUID, com.simul.user.application.dto.UserResponse> userMap = java.util.Map.of(comment.getUserId(), userProfile);
        return createCommentResponse(comment, List.of(), userMap);
    }

    private CommentResponse createCommentResponse(Comment comment, List<CommentResponse> replies, 
                                                java.util.Map<UUID, com.simul.user.application.dto.UserResponse> userMap) {
        boolean isDeleted = comment.getDeletedAt() != null;
        String content = isDeleted ? "삭제된 댓글입니다." : comment.getContent();
        
        var userProfile = userMap.get(comment.getUserId());
        String nickname = userProfile != null ? userProfile.nickname() : "알 수 없는 사용자";
        String profileImageUrl = userProfile != null ? userProfile.profileImageUrl() : null;

        return new CommentResponse(
                comment.getCommentId(),
                comment.getUserId(),
                nickname,
                profileImageUrl,
                content,
                comment.getDepth(),
                comment.getCreatedAt(),
                isDeleted,
                replies
        );
    }
}
