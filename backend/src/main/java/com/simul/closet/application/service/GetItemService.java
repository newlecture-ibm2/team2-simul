package com.simul.closet.application.service;

import com.simul.closet.application.dto.ClosetItemResponse;
import com.simul.closet.application.port.in.GetItemUseCase;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetItemService implements GetItemUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public ClosetItemResponse getItem(UUID itemId) {
        ClosetItem item = closetItemPersistencePort.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 해당 아이템이 속한 컬렉션 ID 목록 조회
        List<UUID> collectionIds = collectionItemPersistencePort.findByItemId(item.getId())
                .stream()
                .map(ci -> ci.getCollection().getId())
                .collect(Collectors.toList());

        return ClosetItemResponse.builder()
                .itemId(item.getId())
                .imageId(item.getClothingImage().getId())
                .imageUrl(item.getClothingImage().getImageUrl())
                .category(item.getCategory())
                .memo(item.getMemo())
                .tryCount(item.getTryCount())
                .collectionIds(collectionIds)
                .createdAt(item.getCreatedAt())
                .build();
    }
}
