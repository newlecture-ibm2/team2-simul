package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.TryonCreditsResponse;
import com.simul.tryon.application.port.in.GetTryonCreditsUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTryonCreditsService implements GetTryonCreditsUseCase {

    private static final int TOTAL_DAILY = 5;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final ZoneOffset KST_OFFSET = ZoneOffset.ofHours(9);

    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;

    @Override
    public TryonCreditsResponse getCredits(GetTryonCreditsQuery query) {
        if (query.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LocalDate todayKst = LocalDate.now(kstClock);
        LocalDateTime start = todayKst.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long usedCount = tryonCreditPersistencePort.countByUserIdAndUsedAtBetween(query.getUserId(), start, end);
        int remaining = Math.max(0, TOTAL_DAILY - (int) usedCount);
        OffsetDateTime resetAt = end.atOffset(KST_OFFSET);

        return new TryonCreditsResponse(remaining, TOTAL_DAILY, resetAt);
    }
}

