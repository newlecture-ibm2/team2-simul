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
import com.simul.tryon.application.port.in.RegisterBaseImageFromPostUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterBaseImageFromPostServiceTest {

    private final PostRepositoryPort postRepositoryPort = mock(PostRepositoryPort.class);
    private final BaseImagePersistencePort baseImagePersistencePort = mock(BaseImagePersistencePort.class);
    private final RegisterBaseImageFromPostService service =
            new RegisterBaseImageFromPostService(postRepositoryPort, baseImagePersistencePort);

    @Test
    void register_requiresSourcePostId() {
        var command = RegisterBaseImageFromPostUseCase.RegisterBaseImageFromPostCommand.builder()
                .userId(UUID.randomUUID())
                .sourcePostId(null)
                .build();

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void register_requiresOwnership() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(postId);
        when(post.getUserId()).thenReturn(UUID.randomUUID());
        when(post.getStatus()).thenReturn(PostStatus.COMPLETED);
        when(post.getImageUrl()).thenReturn("/uploads/images/tryon/result.png");

        when(postRepositoryPort.findById(postId)).thenReturn(Optional.of(post));

        var command = RegisterBaseImageFromPostUseCase.RegisterBaseImageFromPostCommand.builder()
                .userId(userId)
                .sourcePostId(postId)
                .build();

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void register_requiresCompletedPost() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(postId);
        when(post.getUserId()).thenReturn(userId);
        when(post.getStatus()).thenReturn(PostStatus.PROCESSING);
        when(post.getImageUrl()).thenReturn("/uploads/images/tryon/result.png");

        when(postRepositoryPort.findById(postId)).thenReturn(Optional.of(post));

        var command = RegisterBaseImageFromPostUseCase.RegisterBaseImageFromPostCommand.builder()
                .userId(userId)
                .sourcePostId(postId)
                .build();

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void register_savesBaseImageWithSourcePost() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(postId);
        when(post.getUserId()).thenReturn(userId);
        when(post.getStatus()).thenReturn(PostStatus.COMPLETED);
        when(post.getImageUrl()).thenReturn("/uploads/images/tryon/result.png");

        when(postRepositoryPort.findById(postId)).thenReturn(Optional.of(post));

        BaseImage saved = mock(BaseImage.class);
        when(saved.getId()).thenReturn(baseImageId);
        when(saved.getImageUrl()).thenReturn("/uploads/images/tryon/result.png");
        when(baseImagePersistencePort.save(org.mockito.ArgumentMatchers.any(BaseImage.class))).thenReturn(saved);

        var command = RegisterBaseImageFromPostUseCase.RegisterBaseImageFromPostCommand.builder()
                .userId(userId)
                .sourcePostId(postId)
                .build();

        var response = service.register(command);
        assertThat(response.baseImageId()).isEqualTo(baseImageId);
        assertThat(response.imageUrl()).isEqualTo("/uploads/images/tryon/result.png");
    }
}

