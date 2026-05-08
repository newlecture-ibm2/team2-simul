package com.simul.closet.application.service;

import com.simul.closet.application.dto.ClosetCollectionResponse;
import com.simul.closet.application.port.in.GetCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCollectionService implements GetCollectionUseCase {

    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final ClosetItemPersistencePort closetItemPersistencePort;

    @Override
    public ClosetCollectionResponse getCollection(UUID collectionId, UUID userId) {
        ClosetCollection collection = closetCollectionPersistencePort.findById(collectionId);
        if (collection == null || !collection.getUserId().equals(userId)) {
            throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
        }

        List<String> topImages = closetItemPersistencePort.findTopImageUrlsByCollectionId(collectionId, 3);
        long itemCount = closetItemPersistencePort.countByCollectionId(collectionId);

        return ClosetCollectionResponse.builder()
                .collectionId(collection.getId())
                .name(collection.getName())
                .coverImageUrl(collection.getCoverImageUrl())
                .images(topImages)
                .itemCount((int) itemCount)
                .createdAt(collection.getCreatedAt())
                .build();
    }
}
