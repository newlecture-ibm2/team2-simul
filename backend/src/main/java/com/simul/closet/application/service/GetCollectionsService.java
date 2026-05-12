package com.simul.closet.application.service;

import com.simul.closet.adapter.out.persistence.CollectionWithCountDto;
import com.simul.closet.application.dto.ClosetCollectionListResponse;
import com.simul.closet.application.dto.ClosetCollectionResponse;
import com.simul.closet.application.port.in.GetCollectionsUseCase;
import com.simul.closet.application.port.out.ClosetCollectionPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
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
@Transactional
public class GetCollectionsService implements GetCollectionsUseCase {

    private final ClosetCollectionPersistencePort closetCollectionPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    public ClosetCollectionListResponse getCollections(GetCollectionsQuery query) {
        // 1. 해당 유저의 컬렉션이 하나도 없는지 확인
        // 페이지네이션 없이 단순히 존재 여부만 파악하기 위해 findCollectionsWithItemCount 재활용
        Pageable initialCheck = PageRequest.of(0, 1);
        Page<CollectionWithCountDto> collectionsPage = closetCollectionPersistencePort.findCollectionsWithItemCount(
                query.getUserId(),
                initialCheck
        );

        // 2. 하나도 없다면 '기본 폴더' 생성 (요청 사항: 모든 사용자가 반드시 하나씩 가지고 있어야 함)
        if (collectionsPage.getTotalElements() == 0) {
            com.simul.closet.domain.model.ClosetCollection defaultCollection = com.simul.closet.domain.model.ClosetCollection.builder()
                    .userId(query.getUserId())
                    .name("기본 폴더")
                    .sortOrder(0)
                    .build();
            closetCollectionPersistencePort.save(defaultCollection);
            
            // 생성 후 다시 조회
            collectionsPage = closetCollectionPersistencePort.findCollectionsWithItemCount(
                    query.getUserId(),
                    initialCheck // 이후 실제 요청된 pageable로 다시 조회할 것이므로 여기서는 1개만 조회
            );
        }

        // 3. 실제 요청된 정렬 및 페이지네이션으로 조회
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 기본: 최신순
        if ("name".equals(query.getSort())) {
            sort = Sort.by(Sort.Direction.ASC, "name"); // 이름순
        }

        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(), sort);
        collectionsPage = closetCollectionPersistencePort.findCollectionsWithItemCount(
                query.getUserId(),
                pageable
        );

        List<ClosetCollectionResponse> collectionResponses = collectionsPage.getContent().stream()
                .map(dto -> {
                    List<String> topImages = collectionItemPersistencePort.findTopImageUrlsByCollectionId(dto.getCollection().getId(), 3);
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
