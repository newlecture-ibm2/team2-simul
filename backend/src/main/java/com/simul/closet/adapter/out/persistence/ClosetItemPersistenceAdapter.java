package com.simul.closet.adapter.out.persistence;

import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClosetItemPersistenceAdapter implements ClosetItemPersistencePort {

    private final ClosetItemJpaRepository closetItemJpaRepository;

    @Override
    public ClosetItem save(ClosetItem closetItem) {
        return closetItemJpaRepository.save(closetItem);
    }

    @Override
    public Optional<ClosetItem> findById(UUID id) {
        return closetItemJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<ClosetItem> findByUserId(UUID userId) {
        return closetItemJpaRepository.findAllByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Page<ClosetItem> findByUserIdWithFilter(UUID userId, Category category, UUID collectionId, Pageable pageable) {
        return closetItemJpaRepository.findByUserIdAndFiltersWithPaging(userId, category, collectionId, pageable);
    }

    @Override
    public long countByUserId(UUID userId) {
        return closetItemJpaRepository.countByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public void delete(ClosetItem closetItem) {
        closetItem.softDelete();
        closetItemJpaRepository.save(closetItem);
    }

    @Override
    public List<String> findTopImageUrlsByCollectionId(UUID collectionId, int limit) {
        return closetItemJpaRepository.findTopImageUrlsByCollectionId(collectionId, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Override
    public long countByCollectionId(UUID collectionId) {
        return closetItemJpaRepository.countByClosetCollectionIdAndDeletedAtIsNull(collectionId);
    }

    @Override
    public boolean existsInCollection(UUID imageId, UUID collectionId) {
        return closetItemJpaRepository.existsByClothingImageIdAndClosetCollectionIdAndDeletedAtIsNull(imageId, collectionId);
    }
}
