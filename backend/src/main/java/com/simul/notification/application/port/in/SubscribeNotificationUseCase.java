package com.simul.notification.application.port.in;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 실시간 알림 구독 UseCase
 */
public interface SubscribeNotificationUseCase {
    /**
     * 사용자의 실시간 알림을 구독합니다.
     */
    SseEmitter subscribe(UUID userId);
}
