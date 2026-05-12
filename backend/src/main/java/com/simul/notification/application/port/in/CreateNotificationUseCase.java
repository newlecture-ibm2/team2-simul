package com.simul.notification.application.port.in;

import com.simul.notification.domain.model.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 알림 생성 UseCase (Input Port)
 * - 타 도메인(Post, User, TryOn)에서 이벤트 리스너를 통해 호출
 * - 본인 활동에 대한 알림은 생성하지 않음
 */
public interface CreateNotificationUseCase {

    /**
     * 알림을 생성합니다.
     * actorId == recipientId인 경우 (본인 활동) 알림을 생성하지 않고 조용히 무시합니다.
     */
    void createNotification(CreateNotificationCommand command);

    @Getter
    @Builder
    class CreateNotificationCommand {
        /** 행위를 유발한 사용자 ID (좋아요를 누른 사람, 댓글을 단 사람 등) */
        private final UUID actorId;

        /** 알림을 받을 사용자 ID (게시물 작성자, 팔로워 등) */
        private final UUID recipientId;

        /** 알림 유형 */
        private final NotificationType type;

        /** 클릭 시 이동할 대상 ID (게시물 ID 등) */
        private final UUID referenceId;

        /** 알림 메시지 (자동 생성 또는 커스텀) */
        private final String message;
    }
}
