package com.simul.notification.application.dto;

import com.simul.notification.domain.model.Notification;
import com.simul.notification.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 알림 API 응답 DTO
 * - Entity → DTO 변환은 Service 계층에서 수행
 */
public record NotificationResponse(
        UUID notificationId,
        UUID recipientId,
        UUID actorId,
        NotificationType type,
        UUID referenceId,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {

    /**
     * Entity → Response DTO 변환 팩토리 메서드
     */
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getActorId(),
                notification.getType(),
                notification.getReferenceId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
