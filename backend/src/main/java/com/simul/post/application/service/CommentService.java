package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.notification.application.dto.CommentCreatedEvent;
import com.simul.post.application.dto.CommentResponse;
import com.simul.post.application.dto.CreateCommentCommand;
import com.simul.post.application.port.in.CreateCommentUseCase;
import com.simul.post.application.port.in.DeleteCommentUseCase;
import com.simul.post.application.port.in.LoadCommentUseCase;
import com.simul.post.application.port.in.UpdateCommentUseCase;
import com.simul.post.application.port.out.CommentPersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Comment;
import com.simul.post.domain.model.Post;
import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService implements LoadCommentUseCase, CreateCommentUseCase, DeleteCommentUseCase, UpdateCommentUseCase {

    private final CommentPersistencePort commentPersistencePort;
    private final PostRepositoryPort postPersistencePort;
    private final LoadUserUseCase loadUserUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> loadComments(UUID postId, Pageable pageable) {
        // 1. 게시물 존재 확인
        postPersistencePort.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 부모 댓글 조회 (deleted_at 포함 — 대댓글이 있는 삭제된 부모도 보여야 함)
        Page<Comment> parentComments = commentPersistencePort.findParentCommentsByPostId(postId, pageable);

        // 3. 부모 댓글 ID 목록으로 대댓글 일괄 조회 (N+1 해결)
        List<UUID> parentIds = parentComments.getContent().stream()
                .map(Comment::getCommentId)
                .collect(Collectors.toList());

        List<Comment> allReplies = commentPersistencePort.findRepliesByParentIds(parentIds);

        // 대댓글을 부모 ID 기준으로 그룹핑
        Map<UUID, List<Comment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentCommentId));

        // 4. 유저 정보 일괄 조회
        Set<UUID> userIds = new HashSet<>();
        for (Comment c : parentComments.getContent()) {
            if (c.getDeletedAt() == null) {
                userIds.add(c.getUserId());
            }
        }
        for (Comment r : allReplies) {
            if (r.getDeletedAt() == null) {
                userIds.add(r.getUserId());
            }
        }

        Map<UUID, UserResponse> userMap = userIds.isEmpty()
                ? Map.of()
                : loadUserUseCase.loadUsers(new ArrayList<>(userIds));

        // 5. DTO 변환
        return parentComments.map(parent -> {
            boolean parentDeleted = parent.getDeletedAt() != null;

            // 대댓글 중 삭제되지 않은 것만 필터 (depth-2는 자식이 없으므로 삭제되면 숨김)
            List<Comment> replies = repliesMap.getOrDefault(parent.getCommentId(), List.of());
            List<CommentResponse> replyDtos = replies.stream()
                    .filter(r -> r.getDeletedAt() == null)
                    .map(r -> createCommentResponse(r, List.of(), userMap))
                    .collect(Collectors.toList());

            // 삭제된 부모 댓글이지만 살아있는 대댓글이 없으면 → 완전히 숨김 (null 반환 후 필터)
            if (parentDeleted && replyDtos.isEmpty()) {
                return null;
            }

            return createCommentResponse(parent, replyDtos, userMap);
        });
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID postId, UUID userId, CreateCommentCommand command) {
        // 1. 게시물 존재 확인
        Post post = postPersistencePort.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 내용 길이 검증
        if (command.getContent() != null && command.getContent().length() > 200) {
            throw new BusinessException(ErrorCode.COMMENT_TOO_LONG);
        }

        int depth = 1;
        UUID parentCommentId = command.getParentCommentId();

        if (parentCommentId != null) {
            Comment parentComment = commentPersistencePort.findById(parentCommentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

            // 2-depth 제한: depth >= 2인 댓글에 답글 시 최상위 부모로 연결
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

        // 댓글 카운트 증가 (부모 + 대댓글 모두 반영)
        post.incrementCommentCount();
        postPersistencePort.save(post);

        // 3. 알림 이벤트 발행
        eventPublisher.publishEvent(new CommentCreatedEvent(userId, post.getUserId(), postId));

        return mapToResponse(savedComment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentPersistencePort.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 이미 삭제된 댓글 재삭제 방지
        if (comment.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.softDelete();
        commentPersistencePort.save(comment);

        // 댓글 카운트 감소 (부모 + 대댓글 모두 반영)
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

        // 삭제된 댓글 수정 방지
        if (comment.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 200자 제한 검증
        if (content != null && content.length() > 200) {
            throw new BusinessException(ErrorCode.COMMENT_TOO_LONG);
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

        // 삭제된 댓글: 작성자 정보 숨김 (유튜브 스타일)
        String content;
        String nickname;
        String profileImageUrl;

        if (isDeleted) {
            content = "삭제된 댓글입니다.";
            nickname = "";
            profileImageUrl = null;
        } else {
            content = comment.getContent();
            var userProfile = userMap.get(comment.getUserId());
            nickname = userProfile != null ? userProfile.nickname() : "알 수 없는 사용자";
            profileImageUrl = userProfile != null ? userProfile.profileImageUrl() : null;
        }

        return new CommentResponse(
                comment.getCommentId(),
                isDeleted ? null : comment.getUserId(),
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
