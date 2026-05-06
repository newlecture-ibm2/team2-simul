package com.simul.closet.application.port.in;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public interface DeleteItemUseCase {

    void deleteItem(DeleteItemCommand command);

    @Getter
    @Builder
    class DeleteItemCommand {
        private UUID itemId;
        private UUID userId;
    }
}
