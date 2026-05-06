package com.simul.closet.application.service;

import com.simul.closet.application.dto.ClosetItemResponse;
import com.simul.closet.application.port.in.GetItemUseCase;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetItemService implements GetItemUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public ClosetItemResponse getItem(UUID itemId) {
        ClosetItem item = closetItemPersistencePort.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return ClosetItemResponse.builder()
                .itemId(item.getId())
                .imageUrl(item.getClothingImage().getImageUrl())
                .category(item.getCategory())
                .memo(item.getMemo())
                .tryCount(item.getTryCount())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
