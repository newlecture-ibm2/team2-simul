package com.simul.closet.application.port.in;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AddCollectionUseCase {

    UUID addCollection(AddCollectionCommand command);

    @Getter
    class AddCollectionCommand {
        private final UUID userId;
        private final String name;
        private final MultipartFile coverImageFile;

        @Builder
        public AddCollectionCommand(UUID userId, String name, MultipartFile coverImageFile) {
            this.userId = userId;
            this.name = name;
            this.coverImageFile = coverImageFile;
        }
    }
}
