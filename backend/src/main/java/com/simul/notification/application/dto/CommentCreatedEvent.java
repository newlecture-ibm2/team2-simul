package com.simul.notification.application.dto;

import java.util.UUID;

/**
 * 댓글 작성 이벤트
 * - Post 도메인에서 발행, Notification 도메인에서 구독
 * - 댓글 서비스 구현 시 이벤트 발행 코드 추가 필요
 */
public record CommentCreatedEvent(
        /** 댓글을 작성한 사용자 ID */
        UUID actorId,
        /** 게시물 작성자 ID (= 알림 수신자) */
        UUID postOwnerId,
        /** 댓글이 달린 게시물 ID */
        UUID postId
) {}
