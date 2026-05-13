package com.simul.notification.application.port.out;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 실시간 알림 전송을 위한 SSE Output Port
 */
public interface NotificationSsePort {
    /**
     * 특정 사용자의 SSE 연결을 생성합니다.
     */
    SseEmitter subscribe(UUID userId);

    /**
     * 특정 사용자에게 데이터를 전송합니다.
     */
    void send(UUID userId, Object data, String eventName);
}
