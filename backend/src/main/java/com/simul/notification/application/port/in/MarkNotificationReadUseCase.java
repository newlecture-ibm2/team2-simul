package com.simul.notification.application.port.in;

import java.util.UUID;

/**
 * 알림 읽음 처리 UseCase (Input Port)
 * - 개별 읽음: PATCH /notifications/{id}/read
 * - 전체 읽음: PATCH /notifications/read-all
 */
public interface MarkNotificationReadUseCase {

    /**
     * 개별 알림을 읽음 처리합니다.
     * - 본인의 알림만 읽음 처리 가능 (recipientId 검증)
     */
    void markAsRead(UUID notificationId, UUID userId);

    /**
     * 수신자의 모든 미읽음 알림을 읽음 처리합니다.
     * @return 읽음 처리된 알림 수
     */
    int markAllAsRead(UUID userId);
}
