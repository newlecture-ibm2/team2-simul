package com.simul.closet.application.service;

import com.simul.closet.application.port.in.UpdateItemCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.CollectionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateItemCollectionService implements UpdateItemCollectionUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    public void updateItemCollection(UpdateItemCollectionCommand command) {
        ClosetItem item = closetItemPersistencePort.findById(command.itemId())
                .orElseThrow(() -> new RuntimeException("ERR-003: 아이템을 찾을 수 없습니다."));

        if (!item.getUserId().equals(command.userId())) {
            throw new RuntimeException("ERR-002: 권한이 없습니다.");
        }

        if (command.collectionId() != null) {
            ClosetCollection collection = closetCollectionPersistencePort.findById(command.collectionId());
            if (collection == null || !collection.getUserId().equals(command.userId())) {
                throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
            }

            // 중복 체크 후 매핑 추가
            if (!collectionItemPersistencePort.existsByCollectionIdAndItemId(command.collectionId(), command.itemId())) {
                CollectionItem collectionItem = CollectionItem.builder()
                        .collection(collection)
                        .item(item)
                        .sortOrder(0)
                        .build();
                collectionItemPersistencePort.save(collectionItem);
            }
        }
    }

    @Override
    @Transactional
    public void bulkUpdateItemCollection(BulkUpdateItemCollectionCommand command) {
        ClosetCollection collection = null;
        if (command.collectionId() != null) {
            collection = closetCollectionPersistencePort.findById(command.collectionId());
            if (collection == null || !collection.getUserId().equals(command.userId())) {
                throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
            }
        }

        for (UUID itemId : command.itemIds()) {
            ClosetItem item = closetItemPersistencePort.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("ERR-003: 아이템을 찾을 수 없습니다."));

            if (!item.getUserId().equals(command.userId())) {
                throw new RuntimeException("ERR-002: 권한이 없습니다.");
            }

            if (collection != null) {
                // 중복 체크 후 매핑 추가
                if (!collectionItemPersistencePort.existsByCollectionIdAndItemId(command.collectionId(), itemId)) {
                    CollectionItem collectionItem = CollectionItem.builder()
                            .collection(collection)
                            .item(item)
                            .sortOrder(0)
                            .build();
                    collectionItemPersistencePort.save(collectionItem);
                }
            }
        }
    }
}
