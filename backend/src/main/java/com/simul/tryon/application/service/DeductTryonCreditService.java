package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.in.DeductTryonCreditUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import com.simul.tryon.domain.model.TryonCredit;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeductTryonCreditService implements DeductTryonCreditUseCase {

    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;

    @Override
    public void deductOnSuccess(DeductTryonCreditCommand command) {
        if (command.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (command.getJobId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "job_id는 필수입니다.");
        }

        // Idempotency: if already deducted for this job, do nothing.
        if (tryonCreditPersistencePort.existsByJobId(command.getJobId())) {
            return;
        }

        TryonCredit credit = new TryonCredit(command.getUserId(), LocalDateTime.now(kstClock), command.getJobId());
        tryonCreditPersistencePort.save(credit);
    }
}

