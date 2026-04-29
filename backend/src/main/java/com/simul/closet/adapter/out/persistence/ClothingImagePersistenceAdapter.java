package com.simul.closet.adapter.out.persistence;

import com.simul.closet.application.port.out.ClothingImagePersistencePort;
import com.simul.closet.domain.model.ClothingImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClothingImagePersistenceAdapter implements ClothingImagePersistencePort {

    private final ClothingImageJpaRepository clothingImageJpaRepository;

    @Override
    public ClothingImage save(ClothingImage clothingImage) {
        return clothingImageJpaRepository.save(clothingImage);
    }

    @Override
    public Optional<ClothingImage> findById(UUID id) {
        return clothingImageJpaRepository.findById(id);
    }
}
