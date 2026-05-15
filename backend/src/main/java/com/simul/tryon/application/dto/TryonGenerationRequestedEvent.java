package com.simul.tryon.application.dto;

import java.util.List;
import java.util.UUID;

public record TryonGenerationRequestedEvent(
        UUID userId,
        UUID jobId,
        UUID baseImageId,
        List<UUID> itemIds
) {
}

