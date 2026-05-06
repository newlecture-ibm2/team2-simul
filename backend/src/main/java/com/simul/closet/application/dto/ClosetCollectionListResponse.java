package com.simul.closet.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClosetCollectionListResponse {
    private final List<ClosetCollectionResponse> collections;
    private final boolean hasNext;
    private final long totalCount;
}
