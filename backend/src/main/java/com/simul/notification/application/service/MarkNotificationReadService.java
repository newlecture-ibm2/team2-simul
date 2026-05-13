package com.simul.notification.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.notification.application.port.in.MarkNotificationReadUseCase;
import com.simul.notification.application.port.out.NotificationPersistencePort;
import com.simul.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 알림 읽음 처리 서비스 (UseCase 구현체)
 * - 개별 읽음: 엔티티의 markAsRead() 호출 후 저장
 * - 전체 읽음: 벌크 업데이트로 성능 최적화
 */
@Service
@RequiredArgsConstructor
public class MarkNotificationReadService implements MarkNotificationReadUseCase {

    private final NotificationPersistencePort notificationPersistencePort;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "recentNotifications", key = "#userId.toString()"),
        @CacheEvict(value = "unreadCount", key = "#userId.toString()")
    })
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationPersistencePort.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 본인의 알림인지 검증
        if (!notification.getRecipientId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notification.markAsRead();
        notificationPersistencePort.save(notification);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "recentNotifications", key = "#userId.toString()"),
        @CacheEvict(value = "unreadCount", key = "#userId.toString()")
    })
    public int markAllAsRead(UUID userId) {
        return notificationPersistencePort.markAllAsRead(userId);
    }
}
