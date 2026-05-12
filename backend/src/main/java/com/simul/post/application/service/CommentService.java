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
import com.simul.notification.application.port.in.CreateNotificationUseCase;
import com.simul.notification.domain.model.NotificationType;
import com.simul.post.domain.model.Post;
import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

import com.simul.post.application.port.in.UpdateCommentUseCase;

@Service
@RequiredArgsConstructor
public class CommentService implements LoadCommentUseCase, CreateCommentUseCase, DeleteCommentUseCase, UpdateCommentUseCase {

    private final CommentPersistencePort commentPersistencePort;
    private final PostRepositoryPort postPersistencePort;
    private final LoadUserUseCase loadUserUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> loadComments(UUID postId, Pageable pageable) {
        // 1. Check if post exists
        postPersistencePort.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Load Parent Comments
        Page<Comment> parentComments = commentPersistencePort.findParentCommentsByPostId(postId, pageable);

        // 3. 유저 정보 일괄 조회를 위해 ID 수집 및 대댓글 미리 조회
        Set<UUID> userIds = new HashSet<>();
        Map<UUID, List<Comment>> repliesMap = new HashMap<>();

        for (Comment c : parentComments.getContent()) {
            userIds.add(c.getUserId());
            List<Comment> replies = commentPersistencePort.findRepliesByParentId(c.getCommentId());
            repliesMap.put(c.getCommentId(), replies);
            replies.forEach(r -> userIds.add(r.getUserId()));
        }

        // Batch load all users
        Map<UUID, UserResponse> userMap = 
            loadUserUseCase.loadUsers(new ArrayList<>(userIds));

        // 4. Map to DTO
        return parentComments.map(c -> {
            List<Comment> replies = repliesMap.getOrDefault(c.getCommentId(), List.of());
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
        Post post = postPersistencePort.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        int depth = 1;
        UUID parentCommentId = command.getParentCommentId();

        if (parentCommentId != null) {
            Comment parentComment = commentPersistencePort.findById(parentCommentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            
            // Only allow 2 depth
            if (parentComment.getDepth() != null && parentComment.getDepth() >= 2) {
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

        post.incrementCommentCount();
        postPersistencePort.save(post);

        // 알림 생성 로직
        try {
            String nickname = loadUserUseCase.loadUser(userId).nickname();
            createNotificationUseCase.createNotification(
                    CreateNotificationUseCase.CreateNotificationCommand.builder()
                            .actorId(userId)
                            .recipientId(post.getUserId())
                            .type(NotificationType.COMMENT)
                            .referenceId(postId)
                            .message(nickname + "님이 회원님의 게시물에 댓글을 남겼습니다.")
                            .build()
            );
        } catch (Exception e) {
            // 알림 전송 실패가 메인 비즈니스 로직을 방해하지 않도록 처리
        }

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

        Post post = postPersistencePort.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.decrementCommentCount();
            postPersistencePort.save(post);
        }
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID commentId, UUID userId, String content) {
        Comment comment = commentPersistencePort.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.updateContent(content);
        Comment savedComment = commentPersistencePort.save(comment);

        return mapToResponse(savedComment);
    }


    private CommentResponse mapToResponse(Comment comment) {
        UserResponse userProfile = loadUserUseCase.loadUser(comment.getUserId());
        Map<UUID, UserResponse> userMap = Map.of(comment.getUserId(), userProfile);
        return createCommentResponse(comment, List.of(), userMap);
    }

    private CommentResponse createCommentResponse(Comment comment, List<CommentResponse> replies, 
                                                 Map<UUID, UserResponse> userMap) {
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
                comment.getIsEdited() != null ? comment.getIsEdited() : false,
                replies
        );
    }
}
