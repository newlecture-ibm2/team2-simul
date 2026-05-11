package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record TryonCreditsResponse(
        @JsonProperty("remaining") int remaining,
        @JsonProperty("total_daily") int totalDaily,
        @JsonProperty("reset_at") OffsetDateTime resetAt
) {
}

