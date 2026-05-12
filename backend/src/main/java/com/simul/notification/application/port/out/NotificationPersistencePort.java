package com.simul.notification.application.port.out;

import com.simul.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * 알림 도메인 Output Port (Persistence)
 */
public interface NotificationPersistencePort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID notificationId);

    /** 수신자별 알림 목록 (미읽음 우선, 최신순) */
    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    /** 미읽음 알림 수 */
    long countUnreadByRecipientId(UUID recipientId);

    /** 전체 읽음 처리 */
    int markAllAsRead(UUID recipientId);

    /** 개별 알림 삭제 */
    void deleteById(UUID notificationId);

    /** 사용자의 알림 전체 삭제 */
    int deleteAllByRecipientId(UUID recipientId);
}
