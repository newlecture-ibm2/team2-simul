package com.simul.closet.application.port.in;

import java.util.UUID;
import lombok.Builder;

public interface DeleteCollectionUseCase {
    void deleteCollection(DeleteCollectionCommand command);

    @Builder
    record DeleteCollectionCommand(
            UUID userId,
            UUID collectionId
    ) {}
}
