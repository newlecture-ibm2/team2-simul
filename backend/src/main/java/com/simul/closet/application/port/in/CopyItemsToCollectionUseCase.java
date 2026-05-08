package com.simul.closet.application.port.in;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

public interface CopyItemsToCollectionUseCase {
    void copyItemsToCollection(CopyItemsToCollectionCommand command);

    @Builder
    record CopyItemsToCollectionCommand(
            UUID userId,
            List<UUID> sourceItemIds,
            UUID targetCollectionId
    ) {}
}
