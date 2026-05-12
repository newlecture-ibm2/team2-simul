package com.simul.closet.application.port.out;

import com.simul.closet.domain.model.CollectionItem;

import java.util.List;
import java.util.UUID;

public interface CollectionItemPersistencePort {
    CollectionItem save(CollectionItem collectionItem);
    List<CollectionItem> findByCollectionId(UUID collectionId);
    List<CollectionItem> findByItemId(UUID itemId);
    boolean existsByCollectionIdAndItemId(UUID collectionId, UUID itemId);
    void deleteByCollectionIdAndItemId(UUID collectionId, UUID itemId);
    void deleteAllByCollectionId(UUID collectionId);
    void deleteAllByItemId(UUID itemId);
    long countByCollectionId(UUID collectionId);
    List<String> findTopImageUrlsByCollectionId(UUID collectionId, int limit);
}
