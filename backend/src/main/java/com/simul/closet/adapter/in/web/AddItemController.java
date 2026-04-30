package com.simul.closet.adapter.in.web;

import com.simul.closet.application.port.in.AddItemUseCase;
import com.simul.closet.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/closet/items")
@RequiredArgsConstructor
public class AddItemController {

    private final AddItemUseCase addItemUseCase;

    @PostMapping
    public ResponseEntity<UUID> addItem(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "memo", required = false) String memo,
            @RequestParam(value = "collectionId", required = false) UUID collectionId
    ) {
        // TODO: SecurityContext에서 실제 로그인한 유저의 ID를 가져와야 함 (현재는 하드코딩 또는 Mock)
        UUID mockUserId = UUID.randomUUID(); 

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
}
