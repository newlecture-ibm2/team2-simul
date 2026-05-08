package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.BaseImageFromPostRequest;
import com.simul.tryon.application.dto.BaseImageUploadResponse;
import com.simul.tryon.application.port.in.RegisterBaseImageFromPostUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tryon/base-images/from-post")
@RequiredArgsConstructor
public class BaseImageFromPostController {

    private final RegisterBaseImageFromPostUseCase registerBaseImageFromPostUseCase;

    @PostMapping
    public ResponseEntity<BaseImageUploadResponse> registerFromPost(
            @AuthenticationPrincipal UUID userId,
            @RequestBody BaseImageFromPostRequest request
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        BaseImageUploadResponse response = registerBaseImageFromPostUseCase.register(
                RegisterBaseImageFromPostUseCase.RegisterBaseImageFromPostCommand.builder()
                        .userId(userId)
                        .sourcePostId(request.sourcePostId())
                        .build()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

