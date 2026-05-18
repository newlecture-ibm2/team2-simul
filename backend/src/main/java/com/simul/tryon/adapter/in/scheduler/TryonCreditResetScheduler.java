package com.simul.tryon.adapter.in.scheduler;

import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TryonCreditResetScheduler {

    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetDailyCredits() {
        LocalDateTime todayStart = LocalDate.now(kstClock).atStartOfDay();
        int deletedCount = tryonCreditPersistencePort.deleteByUsedAtBefore(todayStart);
        log.info("✅ 시착 크레딧 자정 리셋 완료: deleted={}", deletedCount);
    }
}
