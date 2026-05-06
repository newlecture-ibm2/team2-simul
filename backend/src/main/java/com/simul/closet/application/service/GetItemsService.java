package com.simul.closet.application.service;

import com.simul.closet.application.dto.ClosetItemListResponse;
import com.simul.closet.application.dto.ClosetItemResponse;
import com.simul.closet.application.port.in.GetItemsUseCase;
import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
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
public class GetItemsService implements GetItemsUseCase {

    private final ClosetItemPersistencePort closetItemPersistencePort;

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
        Page<ClosetItem> itemsPage = closetItemPersistencePort.findByUserIdWithFilter(
                query.getUserId(),
                query.getCategory(),
                pageable
        );

        // 4. Response DTO 변환
        List<ClosetItemResponse> itemResponses = itemsPage.getContent().stream()
                .map(item -> ClosetItemResponse.builder()
                        .itemId(item.getId())
                        .imageUrl(item.getClothingImage().getImageUrl())
                        .category(item.getCategory())
                        .memo(item.getMemo())
                        .tryCount(item.getTryCount())
                        .createdAt(item.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ClosetItemListResponse.builder()
                .items(itemResponses)
                .hasNext(itemsPage.hasNext())
                .totalCount(itemsPage.getTotalElements())
                .build();
    }
}
