package com.simul.closet.application.port.in;

import com.simul.closet.domain.model.ClosetCollection;
import java.util.UUID;

public interface GetCollectionUseCase {
    ClosetCollection getCollection(UUID collectionId, UUID userId);
}
