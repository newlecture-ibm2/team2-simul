package com.simul.notification.adapter.in.event;

import com.simul.notification.application.dto.CommentCreatedEvent;
import com.simul.notification.application.dto.PostCreatedEvent;
import com.simul.notification.application.dto.PostLikedEvent;
import com.simul.notification.application.dto.TryonCompletedEvent;
import com.simul.notification.application.port.in.CreateNotificationUseCase;
import com.simul.notification.domain.model.NotificationType;
import com.simul.user.application.port.in.LoadFollowUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 알림 이벤트 리스너 (Input Adapter)
 * - Spring ApplicationEvent를 구독하여 알림 생성 UseCase를 호출
 * - @Async로 비동기 처리하여 메인 트랜잭션에 영향을 주지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final LoadFollowUseCase loadFollowUseCase;

    /**
     * 시착 완료 이벤트 → TRYON_COMPLETE 알림 생성
     * - actorId = null (시스템 알림)
     * - recipientId = 시착 요청자
     * - referenceId = 시착 결과 게시물 ID
     */
    @Async
    @EventListener
    public void handleTryonCompleted(TryonCompletedEvent event) {
        log.info("시착 완료 이벤트 수신: userId={}, postId={}", event.userId(), event.postId());

        createNotificationUseCase.createNotification(
                CreateNotificationUseCase.CreateNotificationCommand.builder()
                        .actorId(null)
                        .recipientId(event.userId())
                        .type(NotificationType.TRYON_COMPLETE)
                        .referenceId(event.postId())
                        .message("AI 시착이 완료되었어요! 결과를 확인해보세요.")
                        .build()
        );
    }

    /**
     * 좋아요 이벤트 → LIKE 알림 생성
     * - 본인 게시물에 본인이 좋아요한 경우 CreateNotificationService에서 자동 스킵
     */
    @Async
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        log.info("좋아요 이벤트 수신: actorId={}, postId={}", event.actorId(), event.postId());

        createNotificationUseCase.createNotification(
                CreateNotificationUseCase.CreateNotificationCommand.builder()
                        .actorId(event.actorId())
                        .recipientId(event.postOwnerId())
                        .type(NotificationType.LIKE)
                        .referenceId(event.postId())
                        .message("회원님의 게시물을 좋아합니다.")
                        .build()
        );
    }

    /**
     * 댓글 작성 이벤트 → COMMENT 알림 생성
     * - 본인 게시물에 본인이 댓글을 단 경우 자동 스킵
     */
    @Async
    @EventListener
    public void handleCommentCreated(CommentCreatedEvent event) {
        log.info("댓글 이벤트 수신: actorId={}, postId={}", event.actorId(), event.postId());

        createNotificationUseCase.createNotification(
                CreateNotificationUseCase.CreateNotificationCommand.builder()
                        .actorId(event.actorId())
                        .recipientId(event.postOwnerId())
                        .type(NotificationType.COMMENT)
                        .referenceId(event.postId())
                        .message("회원님의 게시물에 댓글을 남겼습니다.")
                        .build()
        );
    }

    /**
     * 게시물 작성 이벤트 → FOLLOW_POST 알림 생성 (팔로워 전원)
     * - 작성자의 팔로워 목록을 조회하여 각각 알림 생성
     * - 본인 활동 제외 로직은 팔로워 목록에 본인이 포함될 수 없으므로 자연스럽게 적용됨
     */
    @Async
    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("게시물 작성 이벤트 수신: authorId={}, postId={}", event.authorId(), event.postId());

        // 작성자의 팔로워 목록 조회 (User 도메인의 Input Port 사용)
        List<UUID> followerIds = loadFollowUseCase.getFollowerIds(event.authorId());

        if (followerIds.isEmpty()) {
            log.debug("팔로워가 없어 FOLLOW_POST 알림 생성 스킵: authorId={}", event.authorId());
            return;
        }

        for (UUID followerId : followerIds) {
            createNotificationUseCase.createNotification(
                    CreateNotificationUseCase.CreateNotificationCommand.builder()
                            .actorId(event.authorId())
                            .recipientId(followerId)
                            .type(NotificationType.FOLLOW_POST)
                            .referenceId(event.postId())
                            .message("팔로우한 사용자가 새 게시물을 올렸습니다.")
                            .build()
            );
        }

        log.info("FOLLOW_POST 알림 {}명에게 생성 완료: postId={}", followerIds.size(), event.postId());
    }
}
