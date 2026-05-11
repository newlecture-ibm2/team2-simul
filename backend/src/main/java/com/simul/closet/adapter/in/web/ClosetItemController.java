package com.simul.closet.adapter.in.web;

import com.simul.closet.adapter.in.web.dto.UpdateItemRequest;
import com.simul.closet.application.dto.ClosetItemListResponse;
import com.simul.closet.application.dto.ClosetItemResponse;
import com.simul.closet.application.port.in.AddItemUseCase;
import com.simul.closet.application.port.in.GetItemUseCase;
import com.simul.closet.application.port.in.GetItemsUseCase;
import com.simul.closet.application.port.in.UpdateItemUseCase;
import com.simul.closet.application.port.in.DeleteItemUseCase;
import com.simul.closet.domain.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UpdateItemUseCase updateItemUseCase;
    private final DeleteItemUseCase deleteItemUseCase;
    private final com.simul.closet.application.port.in.UpdateItemCollectionUseCase updateItemCollectionUseCase;
    private final com.simul.closet.application.port.in.CopyItemsToCollectionUseCase copyItemsToCollectionUseCase;

    @PostMapping
    public ResponseEntity<UUID> addItem(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "memo", required = false) String memo,
            @RequestParam(value = "collectionId", required = false) UUID collectionId
    ) {
        log.info("Received request to add item: fileName={}, category={}, memo={}", 
                 imageFile.getOriginalFilename(), category, memo);

        AddItemUseCase.AddItemCommand command = AddItemUseCase.AddItemCommand.builder()
                .userId(userId)
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
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) UUID collectionId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Received request to get items: category={}, collectionId={}, sort={}, page={}, size={}", 
                 category, collectionId, sort, page, size);

        GetItemsUseCase.GetItemsQuery query = GetItemsUseCase.GetItemsQuery.builder()
                .userId(userId)
                .category(category)
                .collectionId(collectionId)
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

    @PatchMapping("/{itemId}")
    public ResponseEntity<Void> updateItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId,
            @RequestBody UpdateItemRequest request
    ) {
        log.info("Received request to update item: itemId={}, category={}, memo={}", 
                 itemId, request.getCategory(), request.getMemo());

        UpdateItemUseCase.UpdateItemCommand command = UpdateItemUseCase.UpdateItemCommand.builder()
                .itemId(itemId)
                .userId(userId)
                .category(request.getCategory())
                .memo(request.getMemo())
                .build();

        updateItemUseCase.updateItem(command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId) {
        log.info("Received request to delete item: itemId={}", itemId);

        DeleteItemUseCase.DeleteItemCommand command = DeleteItemUseCase.DeleteItemCommand.builder()
                .itemId(itemId)
                .userId(userId)
                .build();

        deleteItemUseCase.deleteItem(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{itemId}/collection")
    public ResponseEntity<Void> updateItemCollection(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId,
            @RequestBody com.simul.closet.adapter.in.web.dto.UpdateItemCollectionRequest request
    ) {
        log.info("Received request to update item collection: itemId={}, collectionId={}", 
                 itemId, request.getCollectionId());

        com.simul.closet.application.port.in.UpdateItemCollectionUseCase.UpdateItemCollectionCommand command = com.simul.closet.application.port.in.UpdateItemCollectionUseCase.UpdateItemCollectionCommand.builder()
                .itemId(itemId)
                .userId(userId)
                .collectionId(request.getCollectionId())
                .build();

        updateItemCollectionUseCase.updateItemCollection(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/collection/bulk")
    public ResponseEntity<Void> bulkUpdateItemCollection(
            @AuthenticationPrincipal UUID userId,
            @RequestBody com.simul.closet.adapter.in.web.dto.BulkUpdateItemCollectionRequest request
    ) {
        log.info("Received request to bulk update item collection: itemIds={}, collectionId={}", 
                 request.getItemIds(), request.getCollectionId());

        com.simul.closet.application.port.in.UpdateItemCollectionUseCase.BulkUpdateItemCollectionCommand command = com.simul.closet.application.port.in.UpdateItemCollectionUseCase.BulkUpdateItemCollectionCommand.builder()
                .itemIds(request.getItemIds())
                .userId(userId)
                .collectionId(request.getCollectionId())
                .build();

        updateItemCollectionUseCase.bulkUpdateItemCollection(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/copy")
    public ResponseEntity<Void> copyItemsToCollection(
            @AuthenticationPrincipal UUID userId,
            @RequestBody com.simul.closet.adapter.in.web.dto.BulkUpdateItemCollectionRequest request
    ) {
        log.info("Received request to copy items to collection: itemIds={}, targetCollectionId={}", 
                 request.getItemIds(), request.getCollectionId());

        com.simul.closet.application.port.in.CopyItemsToCollectionUseCase.CopyItemsToCollectionCommand command = com.simul.closet.application.port.in.CopyItemsToCollectionUseCase.CopyItemsToCollectionCommand.builder()
                .sourceItemIds(request.getItemIds())
                .userId(userId)
                .targetCollectionId(request.getCollectionId())
                .build();

        copyItemsToCollectionUseCase.copyItemsToCollection(command);
        return ResponseEntity.ok().build();
    }
}
