package com.simul.notification.adapter.in.web;

import com.simul.notification.application.dto.NotificationResponse;
import com.simul.notification.application.port.in.LoadNotificationUseCase;
import com.simul.notification.application.port.in.MarkNotificationReadUseCase;
import com.simul.notification.application.port.in.SubscribeNotificationUseCase;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * [Hexagonal - Input Adapter (Web)]
 * 알림 REST API 컨트롤러
 *
 * - GET    /notifications              : 알림 목록 조회 (미읽음 우선, 최신순, 페이지네이션)
 * - GET    /notifications/unread-count  : 미읽음 알림 수 조회 (헤더 배지용)
 * - PATCH  /notifications/{id}/read     : 개별 알림 읽음 처리
 * - PATCH  /notifications/read-all      : 전체 알림 읽음 처리
 * - GET    /notifications/subscribe     : 실시간 알림 구독 (SSE)
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final LoadNotificationUseCase loadNotificationUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final SubscribeNotificationUseCase subscribeNotificationUseCase;

    /**
     * 알림 목록 조회
     * - 로그인 필수
     * - 미읽음 우선, 최신순 정렬
     * - 페이지네이션 지원 (page, size)
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        Page<NotificationResponse> notifications = loadNotificationUseCase.getNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 미읽음 알림 수 조회
     * - 로그인 필수
     * - 헤더의 알림 배지 숫자 표시용
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            @AuthenticationPrincipal UUID userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        long unreadCount = loadNotificationUseCase.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of(
                "unread_count", unreadCount
        ));
    }

    /**
     * 개별 알림 읽음 처리
     * - 로그인 필수
     * - 본인의 알림만 읽음 처리 가능
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal UUID userId,
            @PathVariable("id") UUID notificationId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        markNotificationReadUseCase.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 전체 알림 읽음 처리
     * - 로그인 필수
     * - 수신자의 모든 미읽음 알림을 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @AuthenticationPrincipal UUID userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error_code", "ERR-001",
                    "message", "로그인이 필요한 서비스입니다."
            ));
        }

        int updatedCount = markNotificationReadUseCase.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "updated_count", updatedCount
        ));
    }

    /**
     * 실시간 알림 구독 (SSE)
     * - 로그인 필수
     * - 클라이언트는 이 엔드포인트를 통해 서버로부터 실시간 알림을 수신함
     */
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(
            @AuthenticationPrincipal UUID userId
    ) {
        if (userId == null) {
            return null; // 시큐리티에서 처리되겠지만 안전장치
        }
        return subscribeNotificationUseCase.subscribe(userId);
    }
}
