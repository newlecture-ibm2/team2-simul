package com.simul.closet.application.port.in;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import lombok.Builder;

public interface UpdateCollectionUseCase {
    void updateCollection(UpdateCollectionCommand command);

    @Builder
    record UpdateCollectionCommand(
            UUID userId,
            UUID collectionId,
            String name,
            MultipartFile coverImageFile
    ) {}
}
