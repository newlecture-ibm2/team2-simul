package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.TryonCreditsResponse;
import com.simul.tryon.application.port.in.GetTryonCreditsUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tryon/credits")
@RequiredArgsConstructor
public class TryonCreditsController {

    private final GetTryonCreditsUseCase getTryonCreditsUseCase;

    @GetMapping
    public ResponseEntity<TryonCreditsResponse> getCredits(@AuthenticationPrincipal UUID userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        TryonCreditsResponse response = getTryonCreditsUseCase.getCredits(
                GetTryonCreditsUseCase.GetTryonCreditsQuery.builder()
                        .userId(userId)
                        .build()
        );

        return ResponseEntity.ok(response);
    }
}

