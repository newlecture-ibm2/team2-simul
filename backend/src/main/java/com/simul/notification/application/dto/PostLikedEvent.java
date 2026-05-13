package com.simul.notification.application.dto;

import java.util.UUID;

/**
 * 좋아요 이벤트
 * - Post 도메인에서 발행, Notification 도메인에서 구독
 */
public record PostLikedEvent(
        /** 좋아요를 누른 사용자 ID */
        UUID actorId,
        /** 게시물 작성자 ID (= 알림 수신자) */
        UUID postOwnerId,
        /** 좋아요가 눌린 게시물 ID */
        UUID postId
) {}
