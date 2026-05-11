package com.simul.tryon.application.port.out;

import com.simul.tryon.domain.model.TryonCredit;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TryonCreditPersistencePort {
    TryonCredit save(TryonCredit tryonCredit);
    long countByUserIdAndUsedAtBetween(UUID userId, LocalDateTime startInclusive, LocalDateTime endExclusive);
}

