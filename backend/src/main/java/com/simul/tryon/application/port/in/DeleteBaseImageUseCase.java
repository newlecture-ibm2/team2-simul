package com.simul.tryon.application.port.in;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface DeleteBaseImageUseCase {

    void delete(DeleteBaseImageCommand command);

    @Getter
    @Builder
    class DeleteBaseImageCommand {
        private final UUID userId;
        private final UUID baseImageId;
    }
}

