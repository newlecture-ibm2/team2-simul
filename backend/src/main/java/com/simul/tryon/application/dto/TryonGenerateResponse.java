package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record TryonGenerateResponse(
        @JsonProperty("job_id") UUID jobId,
        @JsonProperty("status") String status,
        @JsonProperty("estimated_seconds") int estimatedSeconds
) {
}

