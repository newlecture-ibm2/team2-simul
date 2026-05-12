package com.simul.notification.adapter.out.persistence;

import com.simul.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    /** 알림 목록 조회 — 미읽음 우선, 최신순 정렬 */
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId " +
           "ORDER BY n.isRead ASC, n.createdAt DESC")
    Page<Notification> findByRecipientIdOrderByReadAndCreatedAt(
            @Param("recipientId") UUID recipientId,
            Pageable pageable
    );

    /** 미읽음 알림 수 조회 (배지 표시용) */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.recipientId = :recipientId AND n.isRead = false")
    long countUnreadByRecipientId(@Param("recipientId") UUID recipientId);

    /** 전체 읽음 처리 (벌크 업데이트) */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
           "WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") UUID recipientId);

    /** 사용자의 알림 전체 삭제 */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipientId = :recipientId")
    int deleteAllByRecipientId(@Param("recipientId") UUID recipientId);
}
