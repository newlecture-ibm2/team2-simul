package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.port.in.PublishTryonResultUseCase;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PublishTryonResultServiceTest {

    @Test
    void publish_completedTryonResult_makesPostPublic() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        PublishTryonResultService service = new PublishTryonResultService(postPort);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Post post = mock(Post.class);

        when(postPort.findById(jobId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn(userId);
        when(post.getStatus()).thenReturn(PostStatus.COMPLETED);
        when(post.getImageUrl()).thenReturn("/uploads/images/tryon/result.png");

        service.publish(PublishTryonResultUseCase.PublishTryonResultCommand.builder()
                .userId(userId)
                .jobId(jobId)
                .build());

        verify(post).update(null, true);
        verify(postPort).save(post);
    }

    @Test
    void publish_processingTryonResult_isRejected() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        PublishTryonResultService service = new PublishTryonResultService(postPort);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Post post = mock(Post.class);

        when(postPort.findById(jobId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn(userId);
        when(post.getStatus()).thenReturn(PostStatus.PROCESSING);

        assertThatThrownBy(() -> service.publish(PublishTryonResultUseCase.PublishTryonResultCommand.builder()
                .userId(userId)
                .jobId(jobId)
                .build()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);

        verify(post, never()).update(null, true);
        verify(postPort, never()).save(post);
    }
}
