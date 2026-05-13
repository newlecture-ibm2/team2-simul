package com.simul.notification.application.port.in;

import com.simul.notification.application.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 알림 조회 UseCase (Input Port)
 * - 알림 목록 조회 (미읽음 우선, 최신순)
 * - 미읽음 알림 수 조회 (헤더 배지용)
 */
public interface LoadNotificationUseCase {

    /**
     * 수신자의 알림 목록을 조회합니다.
     * - 미읽음 알림 우선 정렬
     * - 최신순 정렬
     */
    Page<NotificationResponse> getNotifications(UUID recipientId, Pageable pageable);

    /**
     * 수신자의 미읽음 알림 수를 조회합니다. (헤더 배지 표시용)
     */
    long getUnreadCount(UUID recipientId);
}
