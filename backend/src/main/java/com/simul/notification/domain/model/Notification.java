package com.simul.notification.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 알림 엔티티
 * - BaseJpaEntity를 상속하지 않음 (알림은 soft delete 대신 30일 후 물리 삭제 정책)
 * - created_at만 관리하며, updated_at/deleted_at 불필요
 */
@Entity
@Table(name = "notifications", indexes = {
        // 수신자별 최신순 조회 최적화 (목록 조회 API)
        @Index(name = "idx_notifications_recipient_created", columnList = "recipient_id, created_at DESC"),
        // 미읽음 수 조회 최적화 (배지 표시용 count 쿼리)
        @Index(name = "idx_notifications_recipient_unread", columnList = "recipient_id, is_read")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID id;

    /** 알림을 받는 사용자 */
    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    /** 행위를 유발한 사용자 (시스템 알림의 경우 null) */
    @Column(name = "actor_id")
    private UUID actorId;

    /** 알림 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    /** 클릭 시 이동할 대상의 ID (게시물 ID, 사용자 ID 등) */
    @Column(name = "reference_id")
    private UUID referenceId;

    /** 알림 메시지 텍스트 */
    @Column(name = "message", nullable = false, length = 200)
    private String message;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(UUID recipientId, UUID actorId, NotificationType type,
                        UUID referenceId, String message) {
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.type = type;
        this.referenceId = referenceId;
        this.message = message;
        this.isRead = false;
    }

    /** 개별 읽음 처리 */
    public void markAsRead() {
        this.isRead = true;
    }
}
