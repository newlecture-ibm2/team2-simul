package com.simul.closet.application.port.in;

import com.simul.closet.application.dto.ClosetCollectionListResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public interface GetCollectionsUseCase {
    ClosetCollectionListResponse getCollections(GetCollectionsQuery query);

    @Getter
    @Builder
    class GetCollectionsQuery {
        private final UUID userId;
        private final String sort;
        private final int page;
        private final int size;
    }
}
