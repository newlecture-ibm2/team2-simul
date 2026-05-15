package com.simul.tryon.application.port.in;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface PublishTryonResultUseCase {
    void publish(PublishTryonResultCommand command);

    @Getter
    @Builder
    class PublishTryonResultCommand {
        private final UUID userId;
        private final UUID jobId;
    }
}
