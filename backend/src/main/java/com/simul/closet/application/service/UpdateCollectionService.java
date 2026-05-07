package com.simul.closet.application.service;

import com.simul.closet.application.port.in.UpdateCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.common.application.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCollectionService implements UpdateCollectionUseCase {

    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final FileStorageService fileStorageService;

    @Override
    public void updateCollection(UpdateCollectionCommand command) {
        ClosetCollection collection = closetCollectionPersistencePort.findById(command.collectionId());
        
        if (collection == null || !collection.getUserId().equals(command.userId())) {
            throw new RuntimeException("ERR-003: 유효하지 않은 컬렉션입니다.");
        }

        String coverImageUrl = collection.getCoverImageUrl();
        if (command.coverImageFile() != null && !command.coverImageFile().isEmpty()) {
            coverImageUrl = fileStorageService.store(command.coverImageFile());
        }

        collection.update(command.name(), coverImageUrl);
        closetCollectionPersistencePort.save(collection);
    }
}
