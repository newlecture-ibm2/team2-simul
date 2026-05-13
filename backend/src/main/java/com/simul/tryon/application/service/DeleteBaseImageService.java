package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.in.DeleteBaseImageUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteBaseImageService implements DeleteBaseImageUseCase {

    private final BaseImagePersistencePort baseImagePersistencePort;

    @Override
    @Transactional
    public void delete(DeleteBaseImageCommand command) {
        BaseImage baseImage = baseImagePersistencePort.findById(command.getBaseImageId())
                .filter(it -> it.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!baseImage.getUserId().equals(command.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        baseImage.softDelete();
        baseImagePersistencePort.save(baseImage);
    }
}

