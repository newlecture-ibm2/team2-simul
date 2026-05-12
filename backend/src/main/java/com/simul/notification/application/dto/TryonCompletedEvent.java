package com.simul.notification.application.dto;

import java.util.UUID;

/**
 * 시착 완료 이벤트
 * - TryOn 도메인에서 발행, Notification 도메인에서 구독
 * - Spring ApplicationEvent로 도메인 간 느슨한 결합 유지
 */
public record TryonCompletedEvent(
        /** 시착을 요청한 사용자 ID (= 알림 수신자) */
        UUID userId,
        /** 시착 결과 게시물 ID (= 알림 클릭 시 이동 대상) */
        UUID postId
) {}
