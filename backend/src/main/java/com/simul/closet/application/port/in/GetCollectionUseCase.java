package com.simul.closet.application.port.in;

import com.simul.closet.application.dto.ClosetCollectionResponse;
import java.util.UUID;

public interface GetCollectionUseCase {
    ClosetCollectionResponse getCollection(UUID collectionId, UUID userId);
}
