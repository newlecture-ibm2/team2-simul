package com.simul.closet.application.port.in;

import com.simul.closet.domain.model.Category;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AddItemUseCase {
    UUID addItem(AddItemCommand command);

    @Getter
    @Builder
    class AddItemCommand {
        private final UUID userId;
        private final MultipartFile imageFile;
        private final Category category;
        private final String memo;
        private final UUID collectionId;
    }
}
