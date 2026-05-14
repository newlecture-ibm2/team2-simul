package com.simul.tryon.adapter.in.web;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.TryonJobResponse;
import com.simul.tryon.application.port.in.GetTryonJobUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tryon/jobs")
@RequiredArgsConstructor
public class TryonJobController {

    private final GetTryonJobUseCase getTryonJobUseCase;

    @GetMapping("/{jobId}")
    public ResponseEntity<TryonJobResponse> getJob(@AuthenticationPrincipal UUID userId, @PathVariable UUID jobId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        TryonJobResponse response = getTryonJobUseCase.getJob(
                GetTryonJobUseCase.GetTryonJobQuery.builder()
                        .userId(userId)
                        .jobId(jobId)
                        .build()
        );
        return ResponseEntity.ok(response);
    }
}

