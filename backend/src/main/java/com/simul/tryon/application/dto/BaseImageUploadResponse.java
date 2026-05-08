package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record BaseImageUploadResponse(
        @JsonProperty("base_image_id") UUID baseImageId,
        @JsonProperty("image_url") String imageUrl
) {
}

