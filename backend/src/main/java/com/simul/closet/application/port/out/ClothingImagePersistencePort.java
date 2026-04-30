package com.simul.closet.application.port.out;

import com.simul.closet.domain.model.ClothingImage;
import java.util.UUID;
import java.util.Optional;

public interface ClothingImagePersistencePort {
    ClothingImage save(ClothingImage clothingImage);
    Optional<ClothingImage> findById(UUID id);
}
