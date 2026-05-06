package com.simul.closet.application.service;

import com.simul.closet.application.port.in.AddCollectionUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.domain.model.ClosetCollection;
import com.simul.common.application.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddCollectionService implements AddCollectionUseCase {

    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final FileStorageService fileStorageService;

    @Override
    public UUID addCollection(AddCollectionCommand command) {
        String coverImageUrl = null;
        if (command.getCoverImageFile() != null && !command.getCoverImageFile().isEmpty()) {
            coverImageUrl = fileStorageService.store(command.getCoverImageFile());
        }

        // 초기 정렬 순서값은 0으로 설정, 향후 필요시 최대값+1 등 로직 추가 가능
        ClosetCollection collection = ClosetCollection.builder()
                .userId(command.getUserId())
                .name(command.getName())
                .coverImageUrl(coverImageUrl)
                .sortOrder(0)
                .build();

        return closetCollectionPersistencePort.save(collection).getId();
    }
}
