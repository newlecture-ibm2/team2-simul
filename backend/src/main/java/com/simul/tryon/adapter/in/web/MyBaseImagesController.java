package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.MyBaseImagesResponse;
import com.simul.tryon.application.port.in.GetMyBaseImagesUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/base-images")
@RequiredArgsConstructor
public class MyBaseImagesController {

    private final GetMyBaseImagesUseCase getMyBaseImagesUseCase;

    @GetMapping
    public ResponseEntity<MyBaseImagesResponse> getMyBaseImages(@AuthenticationPrincipal UUID userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MyBaseImagesResponse response = getMyBaseImagesUseCase.getMyBaseImages(
                GetMyBaseImagesUseCase.GetMyBaseImagesQuery.builder()
                        .userId(userId)
                        .build()
        );

        return ResponseEntity.ok(response);
    }
}

