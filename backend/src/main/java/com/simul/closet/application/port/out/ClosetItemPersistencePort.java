package com.simul.closet.application.port.out;

import com.simul.closet.domain.model.ClosetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.simul.closet.domain.model.Category;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ClosetItemPersistencePort {
    ClosetItem save(ClosetItem closetItem);
    Optional<ClosetItem> findById(UUID id);
    List<ClosetItem> findByUserId(UUID userId);
    Page<ClosetItem> findByUserIdWithFilter(UUID userId, Category category, UUID collectionId, Pageable pageable);
    long countByUserId(UUID userId);
    void delete(ClosetItem closetItem);
    List<String> findTopImageUrlsByCollectionId(UUID collectionId, int limit);
    long countByCollectionId(UUID collectionId);
    boolean existsInCollection(UUID imageId, UUID collectionId);
}
