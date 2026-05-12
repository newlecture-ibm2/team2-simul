package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.TryonStatusEventResponse;
import com.simul.tryon.application.port.in.GetTryonStatusUseCase;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/tryon/status")
@RequiredArgsConstructor
public class TryonStatusSseController {

    private final GetTryonStatusUseCase getTryonStatusUseCase;

    @GetMapping(value = "/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal UUID userId, @PathVariable UUID jobId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // Keep alive a bit longer than max expected generation time
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(2));
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        emitter.onCompletion(scheduler::shutdownNow);
        emitter.onTimeout(() -> {
            scheduler.shutdownNow();
            emitter.complete();
        });
        emitter.onError(e -> scheduler.shutdownNow());

        scheduler.scheduleAtFixedRate(() -> {
            try {
                TryonStatusEventResponse snapshot = getTryonStatusUseCase.getStatus(
                        GetTryonStatusUseCase.GetTryonStatusQuery.builder()
                                .userId(userId)
                                .jobId(jobId)
                                .build()
                );

                emitter.send(SseEmitter.event()
                        .name(snapshot.status())
                        .data(snapshot));

                if ("completed".equals(snapshot.status()) || "failed".equals(snapshot.status())) {
                    scheduler.shutdownNow();
                    emitter.complete();
                }
            } catch (BusinessException be) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(java.util.Map.of(
                                    "error_code", be.getErrorCode().getCode(),
                                    "message", be.getMessage()
                            )));
                } catch (IOException ignored) {
                }
                scheduler.shutdownNow();
                emitter.completeWithError(be);
            } catch (IOException ioe) {
                scheduler.shutdownNow();
                emitter.completeWithError(ioe);
            } catch (Exception e) {
                scheduler.shutdownNow();
                emitter.completeWithError(e);
            }
        }, 0, 1, TimeUnit.SECONDS);

        return emitter;
    }
}

