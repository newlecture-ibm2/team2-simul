package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.dto.TryonStatusEventResponse;
import com.simul.tryon.application.port.in.GetTryonStatusUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTryonStatusService implements GetTryonStatusUseCase {

    private static final int ESTIMATED_SECONDS = 20;

    private final PostRepositoryPort postRepositoryPort;
    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;

    @Override
    public TryonStatusEventResponse getStatus(GetTryonStatusQuery query) {
        if (query.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (query.getJobId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "job_id는 필수입니다.");
        }

        Post post = postRepositoryPort.findById(query.getJobId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!query.getUserId().equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        PostStatus status = post.getStatus();

        if (status == PostStatus.COMPLETED) {
            boolean deducted = tryonCreditPersistencePort.existsByJobId(post.getPostId());
            return new TryonStatusEventResponse(
                    post.getPostId(),
                    "completed",
                    0,
                    post.getImageUrl(),
                    deducted
            );
        }

        if (status == PostStatus.FAILED) {
            boolean deducted = tryonCreditPersistencePort.existsByJobId(post.getPostId());
            return new TryonStatusEventResponse(
                    post.getPostId(),
                    "failed",
                    0,
                    null,
                    deducted
            );
        }

        // processing
        Integer secondsLeft = calculateEstimatedSecondsLeft(post);
        return new TryonStatusEventResponse(post.getPostId(), "processing", secondsLeft, null, null);
    }

    private Integer calculateEstimatedSecondsLeft(Post post) {
        LocalDateTime createdAt = post.getCreatedAt();
        if (createdAt == null) {
            return ESTIMATED_SECONDS;
        }
        long elapsed = Duration.between(createdAt, LocalDateTime.now(kstClock)).toSeconds();
        long left = ESTIMATED_SECONDS - elapsed;
        return (int) Math.max(0, left);
    }
}

