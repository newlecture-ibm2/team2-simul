package com.simul.closet.application.port.in;

import com.simul.closet.domain.model.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public interface UpdateItemUseCase {

    void updateItem(UpdateItemCommand command);

    @Getter
    @Builder
    class UpdateItemCommand {
        private UUID itemId;
        private UUID userId;
        private Category category;
        private String memo;
    }
}
