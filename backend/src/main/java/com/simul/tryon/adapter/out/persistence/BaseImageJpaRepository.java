package com.simul.tryon.adapter.out.persistence;

import com.simul.tryon.domain.model.BaseImage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseImageJpaRepository extends JpaRepository<BaseImage, UUID> {
    List<BaseImage> findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
