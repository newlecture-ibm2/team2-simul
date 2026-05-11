package com.simul.closet.application.service;

import com.simul.closet.application.port.in.DeleteCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCollectionService implements DeleteCollectionUseCase {

    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    public void deleteCollection(DeleteCollectionCommand command) {
        ClosetCollection collection = closetCollectionPersistencePort.findById(command.collectionId());
        
        if (collection == null || !collection.getUserId().equals(command.userId())) {
            throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
        }

        // 1. 컬렉션에 속한 모든 매핑을 soft delete (아이템 자체는 유지)
        collectionItemPersistencePort.deleteAllByCollectionId(command.collectionId());

        // 2. 컬렉션 자체를 soft delete
        collection.softDelete();
        closetCollectionPersistencePort.save(collection);
    }
}
