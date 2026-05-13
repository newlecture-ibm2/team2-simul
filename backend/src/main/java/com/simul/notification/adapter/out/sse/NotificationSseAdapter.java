package com.simul.notification.adapter.out.sse;

import com.simul.notification.application.port.out.NotificationSsePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE를 통한 실시간 알림 전송 어댑터
 */
@Slf4j
@Component
public class NotificationSseAdapter implements NotificationSsePort {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        
        emitters.put(userId, emitter);

        // 연결 종료 시 처리
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        // 첫 연결 시 더미 데이터 전송 (503 에러 방지)
        send(userId, "connected", "connect");

        return emitter;
    }

    @Override
    public void send(UUID userId, Object data, String eventName) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("SSE 전송 실패: userId={}, message={}", userId, e.getMessage());
            }
        }
    }
}
