package com.simul.notification.domain.model;

/**
 * 알림 유형 ENUM
 * - TRYON_COMPLETE: AI 시착 완료
 * - LIKE: 내 게시물에 좋아요
 * - COMMENT: 내 게시물에 댓글
 * - FOLLOW_POST: 팔로우한 사람의 새 게시물
 */
public enum NotificationType {
    TRYON_COMPLETE,
    LIKE,
    COMMENT,
    FOLLOW_POST
}
