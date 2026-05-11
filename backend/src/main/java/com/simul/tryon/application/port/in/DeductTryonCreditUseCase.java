package com.simul.tryon.application.port.in;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface DeductTryonCreditUseCase {
    void deductOnSuccess(DeductTryonCreditCommand command);

    @Getter
    @Builder
    class DeductTryonCreditCommand {
        private final UUID userId;
        private final UUID jobId;
    }
}

