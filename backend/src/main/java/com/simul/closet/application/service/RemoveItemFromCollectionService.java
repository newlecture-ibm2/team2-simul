package com.simul.closet.application.service;

import com.simul.closet.application.port.in.RemoveItemFromCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.closet.domain.model.ClosetItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RemoveItemFromCollectionService implements RemoveItemFromCollectionUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    public void removeItemFromCollection(RemoveItemFromCollectionCommand command) {
        // 1. 아이템 존재 및 권한 확인
        ClosetItem item = closetItemPersistencePort.findById(command.itemId())
                .orElseThrow(() -> new RuntimeException("ERR-003: 아이템을 찾을 수 없습니다."));

        if (!item.getUserId().equals(command.userId())) {
            throw new RuntimeException("ERR-002: 권한이 없습니다.");
        }

        // 2. 컬렉션 존재 및 권한 확인
        ClosetCollection collection = closetCollectionPersistencePort.findById(command.collectionId());
        if (collection == null || !collection.getUserId().equals(command.userId())) {
            throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
        }

        // 3. 매핑만 삭제 (아이템 자체는 유지)
        collectionItemPersistencePort.deleteByCollectionIdAndItemId(command.collectionId(), command.itemId());
    }
}
