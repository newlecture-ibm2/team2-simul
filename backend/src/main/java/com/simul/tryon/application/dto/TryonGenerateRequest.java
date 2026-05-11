package com.simul.tryon.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record TryonGenerateRequest(
        @JsonProperty("base_image_id") UUID baseImageId,
        @JsonProperty("item_id") UUID itemId,
        @JsonProperty("item_ids") List<UUID> itemIds
) {
}
