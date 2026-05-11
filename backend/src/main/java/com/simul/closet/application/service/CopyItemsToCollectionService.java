package com.simul.closet.application.service;

import com.simul.closet.application.port.in.CopyItemsToCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.ClothingImagePersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.ClothingImage;
import com.simul.closet.domain.model.CollectionItem;
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
    private final CollectionItemPersistencePort collectionItemPersistencePort;
    private final ClothingImagePersistencePort clothingImagePersistencePort;

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

            if (sourceItem.getUserId().equals(command.userId())) {
                // === 같은 소유자: 매핑만 추가 (아이템 복제 없음) ===
                // 중복 체크
                if (collectionItemPersistencePort.existsByCollectionIdAndItemId(
                        command.targetCollectionId(), sourceItem.getId())) {
                    continue; // 이미 해당 컬렉션에 있으면 스킵
                }

                CollectionItem collectionItem = CollectionItem.builder()
                        .collection(targetCollection)
                        .item(sourceItem)
                        .sortOrder(0)
                        .build();
                collectionItemPersistencePort.save(collectionItem);

            } else {
                // === 다른 소유자: Deep Copy (새 아이템 생성 + 매핑) ===
                // 아이템 개수 상한 확인
                long currentCount = closetItemPersistencePort.countByUserId(command.userId());
                if (currentCount >= 200) {
                    throw new RuntimeException("ERR-201-A: 옷장 아이템 개수 상한(200개)을 초과했습니다.");
                }

                // ClothingImage Deep Copy (독립 객체)
                ClothingImage copiedImage = new ClothingImage(
                        sourceItem.getClothingImage().getImageUrl(),
                        command.userId()
                );
                clothingImagePersistencePort.save(copiedImage);

                // ClosetItem Deep Copy
                ClosetItem newItem = ClosetItem.builder()
                        .userId(command.userId())
                        .clothingImage(copiedImage)
                        .category(sourceItem.getCategory())
                        .memo(sourceItem.getMemo())
                        .sortOrder(0)
                        .build();
                ClosetItem savedItem = closetItemPersistencePort.save(newItem);

                // CollectionItem 매핑 생성
                CollectionItem collectionItem = CollectionItem.builder()
                        .collection(targetCollection)
                        .item(savedItem)
                        .sortOrder(0)
                        .build();
                collectionItemPersistencePort.save(collectionItem);
            }
        }
    }
}
