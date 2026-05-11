package com.simul.closet.application.service;

import com.simul.closet.application.dto.ClosetItemListResponse;
import com.simul.closet.application.dto.ClosetItemResponse;
import com.simul.closet.application.port.in.GetItemsUseCase;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.application.port.out.CollectionItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.closet.domain.model.CollectionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetItemsService implements GetItemsUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final CollectionItemPersistencePort collectionItemPersistencePort;

    @Override
    public ClosetItemListResponse getItems(GetItemsQuery query) {
        // 1. 정렬 조건 설정
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // 기본: 최신순
        if ("most_tried".equals(query.getSort())) {
            sort = Sort.by(Sort.Direction.DESC, "tryCount");
        }

        // 2. 페이지네이션 정보 생성
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(), sort);

        // 3. 데이터 조회
        Page<ClosetItem> itemsPage;

        if (query.getCollectionId() != null) {
            // 컬렉션 지정 시: CollectionItem에서 아이템 ID를 먼저 조회
            List<UUID> itemIds = collectionItemPersistencePort.findByCollectionId(query.getCollectionId())
                    .stream()
                    .map(ci -> ci.getItem().getId())
                    .collect(Collectors.toList());

            if (itemIds.isEmpty()) {
                return ClosetItemListResponse.builder()
                        .items(List.of())
                        .hasNext(false)
                        .totalCount(0)
                        .build();
            }

            itemsPage = closetItemPersistencePort.findByIdsWithFilter(
                    itemIds, query.getCategory(), pageable);
        } else {
            // 전체 아이템 조회
            itemsPage = closetItemPersistencePort.findByUserIdWithFilter(
                    query.getUserId(), query.getCategory(), pageable);
        }

        // 4. Response DTO 변환
        List<ClosetItemResponse> itemResponses = itemsPage.getContent().stream()
                .map(item -> {
                    // 해당 아이템이 속한 컬렉션 ID 목록 조회
                    List<UUID> collectionIds = collectionItemPersistencePort.findByItemId(item.getId())
                            .stream()
                            .map(ci -> ci.getCollection().getId())
                            .collect(Collectors.toList());

                    return ClosetItemResponse.builder()
                            .itemId(item.getId())
                            .imageId(item.getClothingImage().getId())
                            .imageUrl(item.getClothingImage().getImageUrl())
                            .category(item.getCategory())
                            .memo(item.getMemo())
                            .tryCount(item.getTryCount())
                            .collectionIds(collectionIds)
                            .createdAt(item.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ClosetItemListResponse.builder()
                .items(itemResponses)
                .hasNext(itemsPage.hasNext())
                .totalCount(itemsPage.getTotalElements())
                .build();
    }
}
