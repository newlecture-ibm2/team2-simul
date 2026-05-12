package com.simul.notification.adapter.out.persistence;

import com.simul.notification.application.port.out.NotificationPersistencePort;
import com.simul.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationPersistencePort {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        return notificationJpaRepository.findById(notificationId);
    }

    @Override
    public Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable) {
        return notificationJpaRepository.findByRecipientIdOrderByReadAndCreatedAt(recipientId, pageable);
    }

    @Override
    public long countUnreadByRecipientId(UUID recipientId) {
        return notificationJpaRepository.countUnreadByRecipientId(recipientId);
    }

    @Override
    public int markAllAsRead(UUID recipientId) {
        return notificationJpaRepository.markAllAsReadByRecipientId(recipientId);
    }

    @Override
    public int deleteOlderThan(LocalDateTime cutoffDate) {
        return notificationJpaRepository.deleteOlderThan(cutoffDate);
    }
}
