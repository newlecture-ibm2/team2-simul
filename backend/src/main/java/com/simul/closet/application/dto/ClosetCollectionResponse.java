package com.simul.closet.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ClosetCollectionResponse {
    private final UUID collectionId;
    private final String name;
    private final String coverImageUrl;
    private final java.util.List<String> images;
    private final int itemCount;
    private final LocalDateTime createdAt;
}
