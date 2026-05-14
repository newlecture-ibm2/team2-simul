package com.simul.tryon.adapter.in.event;

import com.simul.tryon.application.dto.TryonGenerationRequestedEvent;
import com.simul.tryon.application.service.TryonGenerationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * TryOn 생성 요청 이벤트 리스너
 * - 생성 요청은 즉시 응답(201) 후, AFTER_COMMIT 단계에서 비동기 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TryonGenerationEventListener {

    private final TryonGenerationProcessor tryonGenerationProcessor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TryonGenerationRequestedEvent event) {
        log.info("TryOn generation requested: jobId={}, userId={}", event.jobId(), event.userId());
        tryonGenerationProcessor.process(event);
    }
}
