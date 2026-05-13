package com.simul.notification.application.service;

import com.simul.notification.application.port.in.SubscribeNotificationUseCase;
import com.simul.notification.application.port.out.NotificationSsePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 실시간 알림 구독 서비스
 */
@Service
@RequiredArgsConstructor
public class SubscribeNotificationService implements SubscribeNotificationUseCase {

    private final NotificationSsePort notificationSsePort;

    @Override
    public SseEmitter subscribe(UUID userId) {
        return notificationSsePort.subscribe(userId);
    }
}
