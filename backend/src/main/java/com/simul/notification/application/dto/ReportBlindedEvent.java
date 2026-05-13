package com.simul.notification.application.dto;

import java.util.UUID;

/**
 * 게시물이 10회 이상 신고되어 블라인드 처리되었을 때 발생하는 이벤트
 */
public record ReportBlindedEvent(
        UUID postId,
        UUID postOwnerId
) {
}
