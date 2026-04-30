package com.simul.closet.application.port.in;

import com.simul.closet.application.dto.ClosetItemListResponse;
import com.simul.closet.domain.model.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public interface GetItemsUseCase {
    ClosetItemListResponse getItems(GetItemsQuery query);

    @Getter
    @Builder
    class GetItemsQuery {
        private final UUID userId;
        private final Category category;
        private final String sort;
        private final int page;
        private final int size;
    }
}
