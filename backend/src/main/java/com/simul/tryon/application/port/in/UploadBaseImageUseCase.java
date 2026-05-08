package com.simul.tryon.application.port.in;

import com.simul.tryon.application.dto.BaseImageUploadResponse;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public interface UploadBaseImageUseCase {
    BaseImageUploadResponse upload(UploadBaseImageCommand command);

    @Getter
    @Builder
    class UploadBaseImageCommand {
        private final UUID userId;
        private final MultipartFile image;
    }
}

