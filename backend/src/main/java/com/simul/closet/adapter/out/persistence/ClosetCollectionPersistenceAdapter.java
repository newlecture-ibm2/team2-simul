package com.simul.closet.adapter.out.persistence;

import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClosetCollectionPersistenceAdapter implements ClosetCollectionPersistencePort {

    private final ClosetCollectionJpaRepository closetCollectionJpaRepository;

    @Override
    public boolean existsByIdAndUserId(UUID collectionId, UUID userId) {
        return closetCollectionJpaRepository.existsByIdAndUserId(collectionId, userId);
    }

    @Override
    public ClosetCollection findById(UUID id) {
        return closetCollectionJpaRepository.findById(id).orElse(null);
    }

    @Override
    public ClosetCollection save(ClosetCollection collection) {
        return closetCollectionJpaRepository.save(collection);
    }
}
