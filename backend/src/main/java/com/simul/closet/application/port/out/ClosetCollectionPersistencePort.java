package com.simul.closet.application.port.out;

import java.util.UUID;

public interface ClosetCollectionPersistencePort {
    // Collection 엔티티가 완성되면 상세 메서드 추가 예정
    boolean existsByIdAndUserId(UUID collectionId, UUID userId);
    com.simul.closet.domain.model.ClosetCollection findById(UUID id);
    com.simul.closet.domain.model.ClosetCollection save(com.simul.closet.domain.model.ClosetCollection collection);
}
