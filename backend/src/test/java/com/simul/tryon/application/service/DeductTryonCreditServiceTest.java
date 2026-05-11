package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.in.DeductTryonCreditUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeductTryonCreditServiceTest {

    @Test
    void deductOnSuccess_requiresUserId() {
        TryonCreditPersistencePort port = mock(TryonCreditPersistencePort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        DeductTryonCreditService service = new DeductTryonCreditService(port, clock);

        var command = DeductTryonCreditUseCase.DeductTryonCreditCommand.builder()
                .userId(null)
                .jobId(UUID.randomUUID())
                .build();

        assertThatThrownBy(() -> service.deductOnSuccess(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void deductOnSuccess_isIdempotentByJobId() {
        TryonCreditPersistencePort port = mock(TryonCreditPersistencePort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        DeductTryonCreditService service = new DeductTryonCreditService(port, clock);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(port.existsByJobId(jobId)).thenReturn(true);

        var command = DeductTryonCreditUseCase.DeductTryonCreditCommand.builder()
                .userId(userId)
                .jobId(jobId)
                .build();

        service.deductOnSuccess(command);

        verify(port, never()).save(org.mockito.ArgumentMatchers.any());
    }
}

