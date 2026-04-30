package com.simul.closet.adapter.out.persistence;

import com.simul.closet.domain.model.ClothingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClothingImageJpaRepository extends JpaRepository<ClothingImage, UUID> {
}
