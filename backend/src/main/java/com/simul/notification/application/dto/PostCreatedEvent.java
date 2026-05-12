package com.simul.notification.application.dto;

import java.util.UUID;

/**
 * 게시물 작성 이벤트 (공개 게시물만)
 * - Post 도메인에서 발행, Notification 도메인에서 구독
 * - 작성자의 팔로워들에게 FOLLOW_POST 알림 전송
 */
public record PostCreatedEvent(
        /** 게시물 작성자 ID */
        UUID authorId,
        /** 새로 생성된 게시물 ID */
        UUID postId
) {}
