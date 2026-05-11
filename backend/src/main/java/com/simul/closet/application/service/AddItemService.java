package com.simul.closet.application.service;

import com.simul.closet.application.port.in.AddItemUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.ClothingImagePersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.ClothingImage;
import com.simul.closet.domain.model.CollectionItem;
import com.simul.common.application.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddItemService implements AddItemUseCase {

    private final FileStorageService fileStorageService;
    private final ClothingImagePersistencePort clothingImagePersistencePort;
    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    public UUID addItem(AddItemCommand command) {
        // 1. 유저의 현재 옷장 아이템 개수 확인 (상한 200개)
        long currentCount = closetItemPersistencePort.countByUserId(command.getUserId());
        if (currentCount >= 200) {
            throw new RuntimeException("ERR-201-A: 옷장 아이템 개수 상한(200개)을 초과했습니다.");
        }

        // 2. 이미지 저장 및 엔티티 생성
        String imageUrl = fileStorageService.store(command.getImageFile());
        ClothingImage clothingImage = new ClothingImage(imageUrl, command.getUserId());
        clothingImagePersistencePort.save(clothingImage);

        // 3. ClosetItem 생성 및 저장 (컬렉션 정보 없이)
        ClosetItem closetItem = ClosetItem.builder()
                .userId(command.getUserId())
                .clothingImage(clothingImage)
                .category(command.getCategory())
                .memo(command.getMemo())
                .sortOrder(0)
                .build();

        ClosetItem savedItem = closetItemPersistencePort.save(closetItem);

        // 4. 컬렉션이 지정된 경우 CollectionItem 매핑 생성
        if (command.getCollectionId() != null) {
            ClosetCollection collection = closetCollectionPersistencePort.findById(command.getCollectionId());
            if (collection == null || !collection.getUserId().equals(command.getUserId())) {
                throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
            }

            CollectionItem collectionItem = CollectionItem.builder()
                    .collection(collection)
                    .item(savedItem)
                    .sortOrder(0)
                    .build();
            collectionItemPersistencePort.save(collectionItem);
        }

        return savedItem.getId();
    }
}
