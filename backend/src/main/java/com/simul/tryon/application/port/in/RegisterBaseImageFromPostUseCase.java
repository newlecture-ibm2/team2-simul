package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.BaseImageUploadResponse;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

public interface RegisterBaseImageFromPostUseCase {
    BaseImageUploadResponse register(RegisterBaseImageFromPostCommand command);

    @Getter
    @Builder
    class RegisterBaseImageFromPostCommand {
        private final UUID userId;
        private final UUID sourcePostId;
    }
}

