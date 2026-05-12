package com.simul.notification.application.service;

import com.simul.notification.application.dto.NotificationResponse;
import com.simul.notification.application.port.in.LoadNotificationUseCase;
import com.simul.notification.application.port.out.NotificationPersistencePort;
import com.simul.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 알림 조회 서비스 (UseCase 구현체)
 * - 알림 목록 페이지네이션 조회
 * - 미읽음 수 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoadNotificationService implements LoadNotificationUseCase {

    private final NotificationPersistencePort notificationPersistencePort;

    @Override
    public Page<NotificationResponse> getNotifications(UUID recipientId, Pageable pageable) {
        Page<Notification> notifications = notificationPersistencePort.findByRecipientId(recipientId, pageable);
        return notifications.map(NotificationResponse::from);
    }

    @Override
    public long getUnreadCount(UUID recipientId) {
        return notificationPersistencePort.countUnreadByRecipientId(recipientId);
    }
}
