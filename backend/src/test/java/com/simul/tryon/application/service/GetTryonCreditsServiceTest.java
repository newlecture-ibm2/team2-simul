package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.in.GetTryonCreditsUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetTryonCreditsServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    void getCredits_requiresUserId() {
        TryonCreditPersistencePort port = mock(TryonCreditPersistencePort.class);
        Clock clock = Clock.system(KST);
        GetTryonCreditsService service = new GetTryonCreditsService(port, clock);

        var query = GetTryonCreditsUseCase.GetTryonCreditsQuery.builder().userId(null).build();

        assertThatThrownBy(() -> service.getCredits(query))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void getCredits_countsTodayWindowInKstAndCalculatesRemaining() {
        TryonCreditPersistencePort port = mock(TryonCreditPersistencePort.class);

        // 2026-05-11T10:00:00+09:00
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T01:00:00Z"), KST);
        GetTryonCreditsService service = new GetTryonCreditsService(port, clock);

        UUID userId = UUID.randomUUID();

        LocalDateTime start = LocalDateTime.of(2026, 5, 11, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 12, 0, 0);

        when(port.countByUserIdAndUsedAtBetween(userId, start, end)).thenReturn(2L);

        var query = GetTryonCreditsUseCase.GetTryonCreditsQuery.builder().userId(userId).build();
        var response = service.getCredits(query);

        assertThat(response.totalDaily()).isEqualTo(5);
        assertThat(response.remaining()).isEqualTo(3);
        assertThat(response.resetAt().getOffset().getId()).isEqualTo("+09:00");
    }
}
