package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.TryonCreditsResponse;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface GetTryonCreditsUseCase {
    TryonCreditsResponse getCredits(GetTryonCreditsQuery query);

    @Getter
    @Builder
    class GetTryonCreditsQuery {
        private final UUID userId;
    }
}

