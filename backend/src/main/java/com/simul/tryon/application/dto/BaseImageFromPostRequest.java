package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record BaseImageFromPostRequest(
        @JsonProperty("source_post_id") UUID sourcePostId
) {
}

