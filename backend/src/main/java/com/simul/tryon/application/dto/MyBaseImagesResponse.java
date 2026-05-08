package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MyBaseImagesResponse(
        @JsonProperty("base_images") List<BaseImageSummaryResponse> baseImages
) {
}

