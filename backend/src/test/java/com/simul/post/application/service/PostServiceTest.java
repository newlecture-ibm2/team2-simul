package com.simul.post.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simul.common.application.service.FileStorageService;
import com.simul.post.application.port.out.PostLikePersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tag.application.port.in.AttachTagsToPostUseCase;
import com.simul.tag.application.port.in.LoadTagsUseCase;
import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PostServiceTest {

    @Test
    void getPostDetail_usesRepresentativeImageWhenPostImagesAreEmpty() {
        PostRepositoryPort postRepositoryPort = mock(PostRepositoryPort.class);
        PostLikePersistencePort postLikePersistencePort = mock(PostLikePersistencePort.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        AttachTagsToPostUseCase attachTagsToPostUseCase = mock(AttachTagsToPostUseCase.class);
        LoadUserUseCase loadUserUseCase = mock(LoadUserUseCase.class);
        LoadTagsUseCase loadTagsUseCase = mock(LoadTagsUseCase.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .imageUrl("/uploads/images/tryon/result.png")
                .status(PostStatus.COMPLETED)
                .isPublic(true)
                .build();

        when(postRepositoryPort.findById(postId)).thenReturn(Optional.of(post));
        when(loadUserUseCase.loadUser(userId)).thenReturn(new UserResponse(
                userId,
                "tester",
                "tester",
                null,
                null,
                null,
                null,
                true,
                "USER",
                "test"
        ));
        when(loadTagsUseCase.loadTagsByPostIds(List.of(postId))).thenReturn(Map.of(postId, Collections.emptyList()));

        PostService service = new PostService(
                postRepositoryPort,
                postLikePersistencePort,
                fileStorageService,
                attachTagsToPostUseCase,
                loadUserUseCase,
                loadTagsUseCase,
                eventPublisher
        );

        var response = service.getPostDetail(postId, userId);

        assertThat(response.images()).containsExactly("/uploads/images/tryon/result.png");
    }

    @Test
    void getUserPosts_usesProfilePostsForOwner() {
        PostRepositoryPort postRepositoryPort = mock(PostRepositoryPort.class);
        PostLikePersistencePort postLikePersistencePort = mock(PostLikePersistencePort.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        AttachTagsToPostUseCase attachTagsToPostUseCase = mock(AttachTagsToPostUseCase.class);
        LoadUserUseCase loadUserUseCase = mock(LoadUserUseCase.class);
        LoadTagsUseCase loadTagsUseCase = mock(LoadTagsUseCase.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = Post.builder()
                .postId(postId)
                .userId(userId)
                .imageUrl("/uploads/images/post/manual.png")
                .status(PostStatus.COMPLETED)
                .isPublic(false)
                .build();

        when(postRepositoryPort.findProfilePostsByUserId(eq(userId), any()))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(loadUserUseCase.loadUser(userId)).thenReturn(new UserResponse(
                userId,
                "tester",
                "tester",
                null,
                null,
                null,
                null,
                true,
                "USER",
                "test"
        ));
        when(loadTagsUseCase.loadTagsByPostIds(List.of(postId))).thenReturn(Map.of(postId, Collections.emptyList()));
        when(postLikePersistencePort.findLikedPostIdsByUserIdAndPostIds(userId, List.of(postId)))
                .thenReturn(Collections.emptySet());

        PostService service = new PostService(
                postRepositoryPort,
                postLikePersistencePort,
                fileStorageService,
                attachTagsToPostUseCase,
                loadUserUseCase,
                loadTagsUseCase,
                eventPublisher
        );

        var response = service.getUserPosts(userId, userId, PageRequest.of(0, 20));

        assertThat(response.getContent()).hasSize(1);
        verify(postRepositoryPort).findProfilePostsByUserId(eq(userId), any());
    }

    @Test
    void countUserPosts_usesProfilePostCount() {
        PostRepositoryPort postRepositoryPort = mock(PostRepositoryPort.class);
        PostLikePersistencePort postLikePersistencePort = mock(PostLikePersistencePort.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        AttachTagsToPostUseCase attachTagsToPostUseCase = mock(AttachTagsToPostUseCase.class);
        LoadUserUseCase loadUserUseCase = mock(LoadUserUseCase.class);
        LoadTagsUseCase loadTagsUseCase = mock(LoadTagsUseCase.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UUID userId = UUID.randomUUID();
        when(postRepositoryPort.countProfilePostsByUserId(userId)).thenReturn(3L);

        PostService service = new PostService(
                postRepositoryPort,
                postLikePersistencePort,
                fileStorageService,
                attachTagsToPostUseCase,
                loadUserUseCase,
                loadTagsUseCase,
                eventPublisher
        );

        assertThat(service.countUserPosts(userId)).isEqualTo(3L);
        verify(postRepositoryPort).countProfilePostsByUserId(userId);
    }
}
