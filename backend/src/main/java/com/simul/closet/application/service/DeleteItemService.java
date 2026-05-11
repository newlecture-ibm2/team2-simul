package com.simul.closet.application.service;

import com.simul.closet.application.port.in.DeleteItemUseCase;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteItemService implements DeleteItemUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    @Transactional
    public void deleteItem(DeleteItemCommand command) {
        ClosetItem item = closetItemPersistencePort.findById(command.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!item.getUserId().equals(command.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 1. 해당 아이템의 모든 컬렉션 매핑을 soft delete
        collectionItemPersistencePort.deleteAllByItemId(item.getId());

        // 2. 아이템 자체를 soft delete
        item.softDelete();
    }
}
