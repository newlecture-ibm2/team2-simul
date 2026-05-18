package com.simul.tryon.adapter.out.persistence;

import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import com.simul.tryon.domain.model.TryonCredit;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TryonCreditPersistenceAdapter implements TryonCreditPersistencePort {

    private final TryonCreditJpaRepository tryonCreditJpaRepository;

    @Override
    public TryonCredit save(TryonCredit tryonCredit) {
        return tryonCreditJpaRepository.save(tryonCredit);
    }

    @Override
    public long countByUserIdAndUsedAtBetween(UUID userId, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return tryonCreditJpaRepository.countByUserIdAndUsedAtBetween(userId, startInclusive, endExclusive);
    }

    @Override
    public boolean existsByJobId(UUID jobId) {
        return tryonCreditJpaRepository.existsByJobId(jobId);
    }

    @Override
    public void deleteByUserIdAndUsedAtBetween(UUID userId, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        tryonCreditJpaRepository.deleteByUserIdAndUsedAtBetween(userId, startInclusive, endExclusive);
    }

    @Override
    public int deleteByUsedAtBefore(LocalDateTime cutoffExclusive) {
        return tryonCreditJpaRepository.deleteByUsedAtBefore(cutoffExclusive);
    }
}
