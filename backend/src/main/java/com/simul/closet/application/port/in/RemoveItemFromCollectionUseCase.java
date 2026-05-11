package com.simul.closet.application.port.in;

import lombok.Builder;

import java.util.UUID;

public interface RemoveItemFromCollectionUseCase {
    void removeItemFromCollection(RemoveItemFromCollectionCommand command);

    @Builder
    record RemoveItemFromCollectionCommand(
            UUID userId,
            UUID itemId,
            UUID collectionId
    ) {}
}
