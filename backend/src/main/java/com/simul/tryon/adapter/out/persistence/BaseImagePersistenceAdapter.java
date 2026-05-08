package com.simul.tryon.adapter.out.persistence;

import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseImagePersistenceAdapter implements BaseImagePersistencePort {

    private final BaseImageJpaRepository baseImageJpaRepository;

    @Override
    public BaseImage save(BaseImage baseImage) {
        return baseImageJpaRepository.save(baseImage);
    }

    @Override
    public Optional<BaseImage> findById(UUID id) {
        return baseImageJpaRepository.findById(id);
    }

    @Override
    public List<BaseImage> findAllActiveByUserId(UUID userId) {
        return baseImageJpaRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
    }
}
