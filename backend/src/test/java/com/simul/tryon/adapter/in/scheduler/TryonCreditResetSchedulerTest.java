package com.simul.tryon.adapter.in.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TryonCreditResetSchedulerTest {

    @Test
    void resetDailyCredits_deletesCreditsBeforeTodayStartInKst() {
        TryonCreditPersistencePort tryonCreditPersistencePort = mock(TryonCreditPersistencePort.class);
        Clock fixedKstClock = Clock.fixed(Instant.parse("2026-05-15T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        TryonCreditResetScheduler scheduler = new TryonCreditResetScheduler(tryonCreditPersistencePort, fixedKstClock);

        scheduler.resetDailyCredits();

        verify(tryonCreditPersistencePort).deleteByUsedAtBefore(LocalDateTime.of(2026, 5, 15, 0, 0));
    }
}
