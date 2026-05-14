package com.simul.tryon.application.port.in;

import java.util.UUID;

public interface ProvideTryonCreditsUseCase {
    void provideCredits(UUID userId);
}
