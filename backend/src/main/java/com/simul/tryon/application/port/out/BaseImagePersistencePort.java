package com.simul.tryon.application.port.out;

import com.simul.tryon.domain.model.BaseImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseImagePersistencePort {
    BaseImage save(BaseImage baseImage);
    Optional<BaseImage> findById(UUID id);
    List<BaseImage> findAllActiveByUserId(UUID userId);
}
