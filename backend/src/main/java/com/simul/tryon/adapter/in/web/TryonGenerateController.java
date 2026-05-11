package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.TryonGenerateRequest;
import com.simul.tryon.application.dto.TryonGenerateResponse;
import com.simul.tryon.application.port.in.GenerateTryonUseCase;
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
@RequestMapping("/tryon/generate")
@RequiredArgsConstructor
public class TryonGenerateController {

    private final GenerateTryonUseCase generateTryonUseCase;

    @PostMapping
    public ResponseEntity<TryonGenerateResponse> generate(
            @AuthenticationPrincipal UUID userId,
            @RequestBody TryonGenerateRequest request
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        TryonGenerateResponse response = generateTryonUseCase.generate(
                GenerateTryonUseCase.GenerateTryonCommand.builder()
                        .userId(userId)
                        .baseImageId(request.baseImageId())
                        .itemId(request.itemId())
                        .build()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

