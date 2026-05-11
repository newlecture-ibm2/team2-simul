package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.BaseImageUploadResponse;
import com.simul.tryon.application.port.in.UploadBaseImageUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tryon/base-images")
@RequiredArgsConstructor
public class BaseImageController {

    private final UploadBaseImageUseCase uploadBaseImageUseCase;

    @PostMapping
    public ResponseEntity<BaseImageUploadResponse> uploadBaseImage(
            @AuthenticationPrincipal UUID userId,
            @RequestPart("image") MultipartFile image
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        BaseImageUploadResponse response = uploadBaseImageUseCase.upload(
                UploadBaseImageUseCase.UploadBaseImageCommand.builder()
                        .userId(userId)
                        .image(image)
                        .build()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

