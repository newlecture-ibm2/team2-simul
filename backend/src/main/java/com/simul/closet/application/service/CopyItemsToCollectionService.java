package com.simul.closet.application.service;

import com.simul.closet.application.port.in.CopyItemsToCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.closet.domain.model.ClosetItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CopyItemsToCollectionService implements CopyItemsToCollectionUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;

    @Override
    public void copyItemsToCollection(CopyItemsToCollectionCommand command) {
        // 1. 타겟 컬렉션 존재 여부 및 권한 확인
        ClosetCollection targetCollection = closetCollectionPersistencePort.findById(command.targetCollectionId());
        if (targetCollection == null || !targetCollection.getUserId().equals(command.userId())) {
            throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
        }

        // 2. 소스 아이템들을 타겟 컬렉션으로 복사
        for (UUID sourceItemId : command.sourceItemIds()) {
            ClosetItem sourceItem = closetItemPersistencePort.findById(sourceItemId)
                    .orElseThrow(() -> new RuntimeException("ERR-003: 아이템을 찾을 수 없습니다."));

            // 본인 아이템인지 확인
            if (!sourceItem.getUserId().equals(command.userId())) {
                continue; // 혹은 에러 발생
            }

            // 중복 체크 (해당 폴더에 동일한 이미지가 이미 있는지)
            boolean alreadyExists = closetItemPersistencePort.existsInCollection(
                    sourceItem.getClothingImage().getId(), 
                    command.targetCollectionId()
            );

            if (alreadyExists) {
                continue; // 이미 있으면 스킵
            }

            // 새로운 ClosetItem 생성 (Deep Copy)
            ClosetItem newItem = ClosetItem.builder()
                    .userId(command.userId())
                    .clothingImage(sourceItem.getClothingImage())
                    .closetCollection(targetCollection)
                    .category(sourceItem.getCategory())
                    .memo(sourceItem.getMemo())
                    .sortOrder(0)
                    .build();

            closetItemPersistencePort.save(newItem);
        }
    }
}
