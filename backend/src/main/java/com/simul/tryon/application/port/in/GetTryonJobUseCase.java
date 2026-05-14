package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.TryonJobResponse;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface GetTryonJobUseCase {

    TryonJobResponse getJob(GetTryonJobQuery query);

    @Getter
    @Builder
    class GetTryonJobQuery {
        private UUID userId;
        private UUID jobId;
    }
}

