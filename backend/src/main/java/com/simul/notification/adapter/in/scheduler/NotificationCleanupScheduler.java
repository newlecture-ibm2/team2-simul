package com.simul.notification.adapter.in.scheduler;

import com.simul.notification.adapter.out.persistence.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final NotificationJpaRepository notificationJpaRepository;

    // 보관 기간 90일 (요청에 따라 변경됨)
    private static final int RETENTION_DAYS = 90;

    /**
     * 매일 새벽 4시에 90일이 지난 오래된 알림 데이터를 삭제합니다.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupOldNotifications() {
        log.info("🧹 오래된 알림 데이터 정리 스케줄러 시작...");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        
        try {
            int deletedCount = notificationJpaRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("✅ {}일 이전의 알림 데이터 {}개 삭제 완료", RETENTION_DAYS, deletedCount);
        } catch (Exception e) {
            log.error("❌ 알림 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
