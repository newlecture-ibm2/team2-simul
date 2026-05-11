package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TryonStatusEventResponse(
        @JsonProperty("job_id") UUID jobId,
        @JsonProperty("status") String status,
        @JsonProperty("estimated_seconds_left") Integer estimatedSecondsLeft,
        @JsonProperty("result_image_url") String resultImageUrl,
        @JsonProperty("credit_deducted") Boolean creditDeducted
) {
}

