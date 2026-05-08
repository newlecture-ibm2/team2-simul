package com.simul.closet.application.service;

import com.simul.closet.adapter.out.persistence.CollectionWithCountDto;
import com.simul.closet.application.dto.ClosetCollectionListResponse;
import com.simul.closet.application.dto.ClosetCollectionResponse;
import com.simul.closet.application.port.in.GetCollectionsUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCollectionsService implements GetCollectionsUseCase {

    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final com.simul.closet.application.port.out.ClosetItemPersistencePort closetItemPersistencePort;

    @Override
    public ClosetCollectionListResponse getCollections(GetCollectionsQuery query) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 기본: 최신순
        if ("name".equals(query.getSort())) {
            sort = Sort.by(Sort.Direction.ASC, "name"); // 이름순
        }

        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(), sort);

        Page<CollectionWithCountDto> collectionsPage = closetCollectionPersistencePort.findCollectionsWithItemCount(
                query.getUserId(),
                pageable
        );

        List<ClosetCollectionResponse> collectionResponses = collectionsPage.getContent().stream()
                .map(dto -> {
                    List<String> topImages = closetItemPersistencePort.findTopImageUrlsByCollectionId(dto.getCollection().getId(), 3);
                    return ClosetCollectionResponse.builder()
                        .collectionId(dto.getCollection().getId())
                        .name(dto.getCollection().getName())
                        .coverImageUrl(dto.getCollection().getCoverImageUrl())
                        .images(topImages)
                        .itemCount((int) dto.getItemCount())
                        .createdAt(dto.getCollection().getCreatedAt())
                        .build();
                })
                .collect(Collectors.toList());

        return ClosetCollectionListResponse.builder()
                .collections(collectionResponses)
                .hasNext(collectionsPage.hasNext())
                .totalCount(collectionsPage.getTotalElements())
                .build();
    }
}
