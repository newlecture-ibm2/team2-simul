package com.simul.notification.application.service;

import com.simul.notification.application.port.in.CreateNotificationUseCase;
import com.simul.notification.application.port.out.NotificationPersistencePort;
import com.simul.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateNotificationService implements CreateNotificationUseCase {

    private final NotificationPersistencePort notificationPersistencePort;

    @Override
    public void createNotification(CreateNotificationCommand command) {
        // 핵심 규칙: 본인 활동에 대한 알림은 생성하지 않음
        if (command.getActorId() != null
                && command.getActorId().equals(command.getRecipientId())) {
            log.debug("본인 활동 알림 생성 스킵: actorId={}, type={}",
                    command.getActorId(), command.getType());
            return;
        }

        Notification notification = Notification.builder()
                .recipientId(command.getRecipientId())
                .actorId(command.getActorId())
                .type(command.getType())
                .referenceId(command.getReferenceId())
                .message(command.getMessage())
                .build();

        notificationPersistencePort.save(notification);

        log.info("알림 생성 완료: type={}, recipientId={}, actorId={}, referenceId={}",
                command.getType(), command.getRecipientId(),
                command.getActorId(), command.getReferenceId());
    }
}
