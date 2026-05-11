package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClothingImage;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.application.port.out.ImageReadPort;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.tryon.application.port.out.TryonAiGenerationPort;
import com.simul.tryon.application.port.in.GenerateTryonUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GenerateTryonServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    void generate_rejectsWhenCreditsExhausted() {
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T01:00:00Z"), KST);

        when(creditPort.countByUserIdAndUsedAtBetween(any(), any(), any())).thenReturn(5L);

        GenerateTryonService service =
                new GenerateTryonService(baseImagePort, itemPort, postPort, creditPort, clock, imageReadPort, aiPort);

        var command = GenerateTryonUseCase.GenerateTryonCommand.builder()
                .userId(UUID.randomUUID())
                .baseImageId(UUID.randomUUID())
                .itemId(UUID.randomUUID())
                .build();

        assertThatThrownBy(() -> service.generate(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CREDIT_EXHAUSTED);
    }

    @Test
    void generate_createsProcessingJob() {
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T01:00:00Z"), KST);

        UUID userId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(creditPort.countByUserIdAndUsedAtBetween(any(), any(), any())).thenReturn(0L);

        BaseImage baseImage = mock(BaseImage.class);
        when(baseImage.getId()).thenReturn(baseImageId);
        when(baseImage.getUserId()).thenReturn(userId);
        when(baseImagePort.findById(baseImageId)).thenReturn(Optional.of(baseImage));

        ClosetItem item = mock(ClosetItem.class);
        when(item.getId()).thenReturn(itemId);
        when(item.getUserId()).thenReturn(userId);
        ClothingImage clothingImage = mock(ClothingImage.class);
        when(clothingImage.getImageUrl()).thenReturn("/uploads/images/closet/dummy.png");
        when(item.getClothingImage()).thenReturn(clothingImage);
        when(itemPort.findById(itemId)).thenReturn(Optional.of(item));

        Post saved = mock(Post.class);
        when(saved.getPostId()).thenReturn(jobId);
        when(postPort.save(any(Post.class))).thenReturn(saved);

        GenerateTryonService service =
                new GenerateTryonService(baseImagePort, itemPort, postPort, creditPort, clock, imageReadPort, aiPort);

        var command = GenerateTryonUseCase.GenerateTryonCommand.builder()
                .userId(userId)
                .baseImageId(baseImageId)
                .itemId(itemId)
                .build();

        var response = service.generate(command);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.status()).isEqualTo("processing");
        assertThat(response.estimatedSeconds()).isEqualTo(20);
    }

    @Test
    void generate_acceptsOrderedItemIds() {
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T01:00:00Z"), KST);

        UUID userId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        UUID topItemId = UUID.randomUUID();
        UUID bottomItemId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(creditPort.countByUserIdAndUsedAtBetween(any(), any(), any())).thenReturn(0L);

        BaseImage baseImage = mock(BaseImage.class);
        when(baseImage.getId()).thenReturn(baseImageId);
        when(baseImage.getUserId()).thenReturn(userId);
        when(baseImagePort.findById(baseImageId)).thenReturn(Optional.of(baseImage));

        ClosetItem top = mock(ClosetItem.class);
        when(top.getId()).thenReturn(topItemId);
        when(top.getUserId()).thenReturn(userId);
        ClothingImage topImage = mock(ClothingImage.class);
        when(topImage.getImageUrl()).thenReturn("/uploads/images/closet/top.png");
        when(top.getClothingImage()).thenReturn(topImage);
        when(itemPort.findById(topItemId)).thenReturn(Optional.of(top));

        ClosetItem bottom = mock(ClosetItem.class);
        when(bottom.getId()).thenReturn(bottomItemId);
        when(bottom.getUserId()).thenReturn(userId);
        ClothingImage bottomImage = mock(ClothingImage.class);
        when(bottomImage.getImageUrl()).thenReturn("/uploads/images/closet/bottom.png");
        when(bottom.getClothingImage()).thenReturn(bottomImage);
        when(itemPort.findById(bottomItemId)).thenReturn(Optional.of(bottom));

        Post saved = mock(Post.class);
        when(saved.getPostId()).thenReturn(jobId);
        when(postPort.save(any(Post.class))).thenReturn(saved);

        GenerateTryonService service =
                new GenerateTryonService(baseImagePort, itemPort, postPort, creditPort, clock, imageReadPort, aiPort);

        var command = GenerateTryonUseCase.GenerateTryonCommand.builder()
                .userId(userId)
                .baseImageId(baseImageId)
                .itemIds(List.of(topItemId, bottomItemId))
                .build();

        var response = service.generate(command);
        assertThat(response.jobId()).isEqualTo(jobId);
    }

    @Test
    void generate_rejectsMoreThanThreeItemIds() {
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T01:00:00Z"), KST);

        GenerateTryonService service =
                new GenerateTryonService(baseImagePort, itemPort, postPort, creditPort, clock, imageReadPort, aiPort);

        var command = GenerateTryonUseCase.GenerateTryonCommand.builder()
                .userId(UUID.randomUUID())
                .baseImageId(UUID.randomUUID())
                .itemIds(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
                .build();

        assertThatThrownBy(() -> service.generate(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void generate_rejectsNullInItemIds() {
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        TryonCreditPersistencePort creditPort = mock(TryonCreditPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-11T01:00:00Z"), KST);

        GenerateTryonService service =
                new GenerateTryonService(baseImagePort, itemPort, postPort, creditPort, clock, imageReadPort, aiPort);

        var command = GenerateTryonUseCase.GenerateTryonCommand.builder()
                .userId(UUID.randomUUID())
                .baseImageId(UUID.randomUUID())
                .itemIds(java.util.Arrays.asList(UUID.randomUUID(), null))
                .build();

        assertThatThrownBy(() -> service.generate(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }
}
