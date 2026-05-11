package com.simul.closet.application.port.in;

import lombok.Builder;

import java.util.UUID;

public interface UpdateItemCollectionUseCase {
    void updateItemCollection(UpdateItemCollectionCommand command);
    void bulkUpdateItemCollection(BulkUpdateItemCollectionCommand command);

    @Builder
    record UpdateItemCollectionCommand(
            UUID userId,
            UUID itemId,
            UUID collectionId
    ) {}

    @Builder
    record BulkUpdateItemCollectionCommand(
            UUID userId,
            java.util.List<UUID> itemIds,
            UUID collectionId
    ) {}
}
