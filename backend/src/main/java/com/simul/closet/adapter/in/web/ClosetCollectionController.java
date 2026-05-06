package com.simul.closet.adapter.in.web;

import com.simul.closet.application.port.in.AddCollectionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/closet/collections")
@RequiredArgsConstructor
public class ClosetCollectionController {

    private final AddCollectionUseCase addCollectionUseCase;

    @PostMapping
    public ResponseEntity<UUID> addCollection(
            @RequestParam("name") String name,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile
    ) {
        log.info("Received request to add collection: name={}", name);

        // TODO: SecurityContext에서 실제 로그인한 유저의 ID를 가져와야 함
        UUID mockUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        AddCollectionUseCase.AddCollectionCommand command = AddCollectionUseCase.AddCollectionCommand.builder()
                .userId(mockUserId)
                .name(name)
                .coverImageFile(coverImageFile)
                .build();

        UUID collectionId = addCollectionUseCase.addCollection(command);
        return ResponseEntity.ok(collectionId);
    }
}
