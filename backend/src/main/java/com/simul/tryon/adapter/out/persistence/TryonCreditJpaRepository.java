package com.simul.tryon.adapter.out.persistence;

import com.simul.tryon.domain.model.TryonCredit;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TryonCreditJpaRepository extends JpaRepository<TryonCredit, UUID> {
    long countByUserIdAndUsedAtBetween(UUID userId, LocalDateTime startInclusive, LocalDateTime endExclusive);
}

