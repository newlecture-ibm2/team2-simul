package com.simul.closet.adapter.out.persistence;

import com.simul.closet.domain.model.ClosetCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClosetCollectionJpaRepository extends JpaRepository<ClosetCollection, UUID> {
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
