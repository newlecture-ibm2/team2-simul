package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.port.in.GetTryonStatusUseCase;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetTryonStatusServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    void getStatus_requiresOwnership() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T00:00:00Z"), KST);

        UUID jobId = UUID.randomUUID();
        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(jobId);
        when(post.getUserId()).thenReturn(UUID.randomUUID());
        when(post.getStatus()).thenReturn(PostStatus.PROCESSING);
        when(postPort.findById(jobId)).thenReturn(Optional.of(post));

        GetTryonStatusService service = new GetTryonStatusService(postPort, creditPort, clock);

        var query = GetTryonStatusUseCase.GetTryonStatusQuery.builder()
                .userId(UUID.randomUUID())
                .jobId(jobId)
                .build();

        assertThatThrownBy(() -> service.getStatus(query))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void getStatus_completedIncludesResultAndCreditDeducted() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T00:00:00Z"), KST);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(jobId);
        when(post.getUserId()).thenReturn(userId);
        when(post.getStatus()).thenReturn(PostStatus.COMPLETED);
        when(post.getImageUrl()).thenReturn("/uploads/images/tryon/result.png");
        when(postPort.findById(jobId)).thenReturn(Optional.of(post));
        when(creditPort.existsByJobId(jobId)).thenReturn(true);

        GetTryonStatusService service = new GetTryonStatusService(postPort, creditPort, clock);

        var query = GetTryonStatusUseCase.GetTryonStatusQuery.builder()
                .userId(userId)
                .jobId(jobId)
                .build();

        var response = service.getStatus(query);
        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.resultImageUrl()).isEqualTo("/uploads/images/tryon/result.png");
        assertThat(response.creditDeducted()).isTrue();
        assertThat(response.estimatedSecondsLeft()).isEqualTo(0);
    }

    @Test
    void getStatus_processingCalculatesSecondsLeft() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T00:00:10Z"), KST);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(jobId);
        when(post.getUserId()).thenReturn(userId);
        when(post.getStatus()).thenReturn(PostStatus.PROCESSING);
        when(post.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 5, 11, 9, 0, 0)); // 2026-05-11T09:00:00+09
        when(postPort.findById(jobId)).thenReturn(Optional.of(post));

        GetTryonStatusService service = new GetTryonStatusService(postPort, creditPort, clock);

        var query = GetTryonStatusUseCase.GetTryonStatusQuery.builder()
                .userId(userId)
                .jobId(jobId)
                .build();

        var response = service.getStatus(query);
        assertThat(response.status()).isEqualTo("processing");
        assertThat(response.estimatedSecondsLeft()).isBetween(0, 20);
    }
}

