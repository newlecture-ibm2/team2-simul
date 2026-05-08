package com.simul.tryon.application.service;

import com.simul.common.application.port.out.ImageStoragePort;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.common.utils.ImageUtils;
import com.simul.tryon.application.dto.BaseImageUploadResponse;
import com.simul.tryon.application.port.in.UploadBaseImageUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UploadBaseImageService implements UploadBaseImageUseCase {

    private static final String DIRECTORY_PREFIX = "tryon";
    private static final long MAX_IMAGE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final int MIN_WIDTH = 256;
    private static final int MIN_HEIGHT = 256;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/heic"
    );

    private final ImageStoragePort imageStoragePort;
    private final BaseImagePersistencePort baseImagePersistencePort;

    @Override
    public BaseImageUploadResponse upload(UploadBaseImageCommand command) {
        if (command.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        validateBaseImage(command);

        String imageUrl = imageStoragePort.uploadImage(command.getImage(), DIRECTORY_PREFIX);
        BaseImage saved = baseImagePersistencePort.save(new BaseImage(command.getUserId(), imageUrl));

        return new BaseImageUploadResponse(saved.getId(), saved.getImageUrl());
    }

    private void validateBaseImage(UploadBaseImageCommand command) {
        ImageUtils.validateFormat(command.getImage());

        String contentType = command.getImage().getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "지원하지 않는 이미지 포맷입니다. (JPG, PNG, HEIC만 허용)"
            );
        }

        ImageUtils.validateSize(command.getImage(), MAX_IMAGE_SIZE_BYTES, ErrorCode.INVALID_INPUT);
        ImageUtils.validateResolution(command.getImage(), MIN_WIDTH, MIN_HEIGHT);
    }
}
