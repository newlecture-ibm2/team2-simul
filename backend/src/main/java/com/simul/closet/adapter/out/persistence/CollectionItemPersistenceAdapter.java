package com.simul.closet.adapter.out.persistence;

import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.CollectionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CollectionItemPersistenceAdapter implements CollectionItemPersistencePort {

    private final CollectionItemJpaRepository collectionItemJpaRepository;

    @Override
    public CollectionItem save(CollectionItem collectionItem) {
        return collectionItemJpaRepository.save(collectionItem);
    }

    @Override
    public List<CollectionItem> findByCollectionId(UUID collectionId) {
        return collectionItemJpaRepository.findByCollectionIdAndDeletedAtIsNull(collectionId);
    }

    @Override
    public List<CollectionItem> findByItemId(UUID itemId) {
        return collectionItemJpaRepository.findByItemIdAndDeletedAtIsNull(itemId);
    }

    @Override
    public boolean existsByCollectionIdAndItemId(UUID collectionId, UUID itemId) {
        return collectionItemJpaRepository.existsByCollectionIdAndItemIdAndDeletedAtIsNull(collectionId, itemId);
    }

    @Override
    public void deleteByCollectionIdAndItemId(UUID collectionId, UUID itemId) {
        collectionItemJpaRepository.softDeleteByCollectionIdAndItemId(collectionId, itemId);
    }

    @Override
    public void deleteAllByCollectionId(UUID collectionId) {
        collectionItemJpaRepository.softDeleteAllByCollectionId(collectionId);
    }

    @Override
    public void deleteAllByItemId(UUID itemId) {
        collectionItemJpaRepository.softDeleteAllByItemId(itemId);
    }

    @Override
    public long countByCollectionId(UUID collectionId) {
        return collectionItemJpaRepository.countByCollectionIdAndDeletedAtIsNull(collectionId);
    }

    @Override
    public List<String> findTopImageUrlsByCollectionId(UUID collectionId, int limit) {
        return collectionItemJpaRepository.findTopImageUrlsByCollectionId(collectionId, PageRequest.of(0, limit));
    }
}
