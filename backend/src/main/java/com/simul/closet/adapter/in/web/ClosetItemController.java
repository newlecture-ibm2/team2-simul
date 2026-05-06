package com.simul.closet.adapter.in.web;

import com.simul.closet.application.dto.ClosetItemListResponse;
import com.simul.closet.application.dto.ClosetItemResponse;
import com.simul.closet.application.port.in.AddItemUseCase;
import com.simul.closet.application.port.in.GetItemUseCase;
import com.simul.closet.application.port.in.GetItemsUseCase;
import com.simul.closet.domain.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/closet/items")
@RequiredArgsConstructor
public class ClosetItemController {

    private final AddItemUseCase addItemUseCase;
    private final GetItemsUseCase getItemsUseCase;
    private final GetItemUseCase getItemUseCase;

    @PostMapping
    public ResponseEntity<UUID> addItem(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "memo", required = false) String memo,
            @RequestParam(value = "collectionId", required = false) UUID collectionId
    ) {
        log.info("Received request to add item: fileName={}, category={}, memo={}", 
                 imageFile.getOriginalFilename(), category, memo);

        // TODO: SecurityContext에서 실제 로그인한 유저의 ID를 가져와야 함
        UUID mockUserId = UUID.fromString("00000000-0000-0000-0000-000000000001"); 

        AddItemUseCase.AddItemCommand command = AddItemUseCase.AddItemCommand.builder()
                .userId(mockUserId)
                .imageFile(imageFile)
                .category(category)
                .memo(memo)
                .collectionId(collectionId)
                .build();

        UUID itemId = addItemUseCase.addItem(command);
        return ResponseEntity.ok(itemId);
    }

    @GetMapping
    public ResponseEntity<ClosetItemListResponse> getItems(
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Received request to get items: category={}, sort={}, page={}, size={}", 
                 category, sort, page, size);

        // TODO: SecurityContext에서 실제 로그인한 유저의 ID를 가져와야 함
        UUID mockUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        GetItemsUseCase.GetItemsQuery query = GetItemsUseCase.GetItemsQuery.builder()
                .userId(mockUserId)
                .category(category)
                .sort(sort)
                .page(page)
                .size(size)
                .build();

        ClosetItemListResponse response = getItemsUseCase.getItems(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ClosetItemResponse> getItem(@PathVariable UUID itemId) {
        log.info("Received request to get item: itemId={}", itemId);
        ClosetItemResponse response = getItemUseCase.getItem(itemId);
        return ResponseEntity.ok(response);
    }
}
