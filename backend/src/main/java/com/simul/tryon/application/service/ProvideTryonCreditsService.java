package com.simul.tryon.application.service;

import com.simul.tryon.application.port.in.ProvideTryonCreditsUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProvideTryonCreditsService implements ProvideTryonCreditsUseCase {

    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;

    @Override
    @Transactional
    public void provideCredits(UUID userId) {
        LocalDate todayKst = LocalDate.now(kstClock);
        LocalDateTime start = todayKst.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        tryonCreditPersistencePort.deleteByUserIdAndUsedAtBetween(userId, start, end);
    }
}
