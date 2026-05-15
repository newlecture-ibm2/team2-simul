package com.simul.user.adapter.in.scheduler;

import com.simul.auth.adapter.out.persistence.EmailVerificationJpaRepository;
import com.simul.user.adapter.out.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnverifiedUserCleanupScheduler {

    private final UserJpaRepository userJpaRepository;
    private final EmailVerificationJpaRepository emailVerificationJpaRepository;

    /**
     * 매일 새벽 3시에 24시간이 지난 미인증 계정 및 만료된 토큰을 삭제합니다.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.info("🧹 미인증 계정 및 만료된 토큰 정리 스케줄러 시작...");
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
        
        try {
            // 1. 만료된 인증 토큰 삭제 (현재 시간 기준)
            int deletedTokens = emailVerificationJpaRepository.deleteByExpiryDateBefore(LocalDateTime.now());
            
            // 2. 생성된 지 24시간이 지난 미인증 가계정(isActive=false) 삭제
            int deletedUsers = userJpaRepository.deleteByIsActiveFalseAndCreatedAtBefore(cutoffDate);
            
            log.info("✅ 만료된 인증 토큰 {}개, 미인증 가계정 {}개 삭제 완료", deletedTokens, deletedUsers);
        } catch (Exception e) {
            log.error("❌ 미인증 계정 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
