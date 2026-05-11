package com.simul.tryon.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.simul.tryon.domain.model.TryonCredit;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TryonCreditJpaRepositoryTest {

    @Autowired
    private TryonCreditJpaRepository tryonCreditJpaRepository;

    @Test
    void countByUserIdAndUsedAtBetween_countsWithinWindow() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        tryonCreditJpaRepository.save(new TryonCredit(userId, now.minusHours(2), jobId));
        tryonCreditJpaRepository.save(new TryonCredit(userId, now.minusMinutes(10), UUID.randomUUID()));
        tryonCreditJpaRepository.save(new TryonCredit(UUID.randomUUID(), now.minusMinutes(10), UUID.randomUUID()));

        long count = tryonCreditJpaRepository.countByUserIdAndUsedAtBetween(
                userId,
                now.minusHours(1),
                now.plusHours(1)
        );

        assertThat(count).isEqualTo(1);
    }
}

