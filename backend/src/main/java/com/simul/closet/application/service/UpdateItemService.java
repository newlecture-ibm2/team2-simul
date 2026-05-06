package com.simul.closet.application.service;

import com.simul.closet.application.port.in.UpdateItemUseCase;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.Category;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateItemService implements UpdateItemUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;

    @Override
    @Transactional
    public void updateItem(UpdateItemCommand command) {
        ClosetItem item = closetItemPersistencePort.findById(command.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!item.getUserId().equals(command.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Category newCategory = command.getCategory() != null ? command.getCategory() : item.getCategory();
        String newMemo = command.getMemo() != null ? command.getMemo() : item.getMemo();

        item.update(newCategory, newMemo);
    }
}
