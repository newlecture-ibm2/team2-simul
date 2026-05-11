package com.simul.closet.adapter.in.web;

import com.simul.closet.application.dto.ClosetCollectionListResponse;
import com.simul.closet.application.port.in.AddCollectionUseCase;
import com.simul.closet.application.port.in.GetCollectionsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/closet/collections")
@RequiredArgsConstructor
public class ClosetCollectionController {

    private final AddCollectionUseCase addCollectionUseCase;
    private final GetCollectionsUseCase getCollectionsUseCase;
    private final com.simul.closet.application.port.in.GetCollectionUseCase getCollectionUseCase;
    private final com.simul.closet.application.port.in.UpdateCollectionUseCase updateCollectionUseCase;
    private final com.simul.closet.application.port.in.DeleteCollectionUseCase deleteCollectionUseCase;

    @GetMapping("/{collectionId}")
    public ResponseEntity<com.simul.closet.application.dto.ClosetCollectionResponse> getCollection(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID collectionId) {
        log.info("Received request to get collection detail: id={}", collectionId);

        com.simul.closet.application.dto.ClosetCollectionResponse response = getCollectionUseCase.getCollection(collectionId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UUID> addCollection(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("name") String name,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile
    ) {
        log.info("Received request to add collection: name={}", name);

        AddCollectionUseCase.AddCollectionCommand command = AddCollectionUseCase.AddCollectionCommand.builder()
                .userId(userId)
                .name(name)
                .coverImageFile(coverImageFile)
                .build();

        UUID collectionId = addCollectionUseCase.addCollection(command);
        return ResponseEntity.ok(collectionId);
    }

    @GetMapping
    public ResponseEntity<ClosetCollectionListResponse> getCollections(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Received request to get collections: sort={}, page={}, size={}", sort, page, size);

        GetCollectionsUseCase.GetCollectionsQuery query = GetCollectionsUseCase.GetCollectionsQuery.builder()
                .userId(userId)
                .sort(sort)
                .page(page)
                .size(size)
                .build();

        ClosetCollectionListResponse response = getCollectionsUseCase.getCollections(query);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{collectionId}")
    public ResponseEntity<Void> updateCollection(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID collectionId,
            @RequestParam("name") String name,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile
    ) {
        log.info("Received request to update collection: id={}, name={}", collectionId, name);

        com.simul.closet.application.port.in.UpdateCollectionUseCase.UpdateCollectionCommand command = com.simul.closet.application.port.in.UpdateCollectionUseCase.UpdateCollectionCommand.builder()
                .userId(userId)
                .collectionId(collectionId)
                .name(name)
                .coverImageFile(coverImageFile)
                .build();

        updateCollectionUseCase.updateCollection(command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID collectionId) {
        log.info("Received request to delete collection: id={}", collectionId);

        com.simul.closet.application.port.in.DeleteCollectionUseCase.DeleteCollectionCommand command = com.simul.closet.application.port.in.DeleteCollectionUseCase.DeleteCollectionCommand.builder()
                .userId(userId)
                .collectionId(collectionId)
                .build();

        deleteCollectionUseCase.deleteCollection(command);
        return ResponseEntity.noContent().build();
    }
}
