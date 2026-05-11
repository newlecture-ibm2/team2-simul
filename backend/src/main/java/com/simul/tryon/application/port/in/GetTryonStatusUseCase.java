package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.TryonStatusEventResponse;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface GetTryonStatusUseCase {
    TryonStatusEventResponse getStatus(GetTryonStatusQuery query);

    @Getter
    @Builder
    class GetTryonStatusQuery {
        private final UUID userId;
        private final UUID jobId;
    }
}

