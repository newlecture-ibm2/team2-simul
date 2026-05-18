package com.simul.tryon.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClothingImage;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.application.port.out.BinaryImageStoragePort;
import com.simul.common.application.port.out.ImageReadPort;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.notification.application.dto.TryonCompletedEvent;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.tryon.application.dto.TryonGenerationRequestedEvent;
import com.simul.tryon.application.port.in.DeductTryonCreditUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.application.port.out.SafeSearchPort;
import com.simul.tryon.application.port.out.TryonAiGenerationPort;
import com.simul.tryon.application.port.out.TryonResultQualityPort;
import com.simul.tryon.domain.model.BaseImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class TryonGenerationProcessorTest {

    @Test
    void process_whenAiGenerationSucceeds_completesPostAndDeductsCredit() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        BinaryImageStoragePort imageStoragePort = mock(BinaryImageStoragePort.class);
        DeductTryonCreditUseCase deductCreditUseCase = mock(DeductTryonCreditUseCase.class);
        SafeSearchPort safeSearchPort = mock(SafeSearchPort.class);
        TryonResultQualityPort tryonResultQualityPort = mock(TryonResultQualityPort.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getUserId()).thenReturn(userId);
        when(post.getPostId()).thenReturn(jobId);
        when(postPort.findById(jobId)).thenReturn(Optional.of(post));

        BaseImage baseImage = mock(BaseImage.class);
        when(baseImage.getUserId()).thenReturn(userId);
        when(baseImage.getImageUrl()).thenReturn("/uploads/images/tryon/base.png");
        when(baseImagePort.findById(baseImageId)).thenReturn(Optional.of(baseImage));

        ClosetItem item = mock(ClosetItem.class);
        ClothingImage clothingImage = mock(ClothingImage.class);
        when(item.getUserId()).thenReturn(userId);
        when(item.getClothingImage()).thenReturn(clothingImage);
        when(clothingImage.getImageUrl()).thenReturn("/uploads/images/closet/top.png");
        when(itemPort.findById(itemId)).thenReturn(Optional.of(item));

        when(imageReadPort.read("/uploads/images/tryon/base.png"))
                .thenReturn(new ImageReadPort.ImageReadResult("base".getBytes(), "image/png"));
        when(imageReadPort.read("/uploads/images/closet/top.png"))
                .thenReturn(new ImageReadPort.ImageReadResult("top".getBytes(), "image/png"));
        when(safeSearchPort.analyze(any()))
                .thenReturn(new SafeSearchPort.SafeSearchResult(
                        SafeSearchPort.Likelihood.VERY_UNLIKELY,
                        SafeSearchPort.Likelihood.VERY_UNLIKELY,
                        SafeSearchPort.Likelihood.VERY_UNLIKELY
                ));
        when(aiPort.generate(any()))
                .thenReturn(new TryonAiGenerationPort.TryonAiGenerationResult("result".getBytes(), "image/png"));
        when(tryonResultQualityPort.validate(any()))
                .thenReturn(new TryonResultQualityPort.TryonResultQualityResult(true, "OK"));
        when(imageStoragePort.upload(any(), any(), any())).thenReturn("/uploads/images/tryon/result.png");

        TryonGenerationProcessor processor = new TryonGenerationProcessor(
                postPort,
                baseImagePort,
                itemPort,
                imageReadPort,
                aiPort,
                imageStoragePort,
                deductCreditUseCase,
                safeSearchPort,
                tryonResultQualityPort,
                eventPublisher
        );

        processor.process(new TryonGenerationRequestedEvent(userId, jobId, baseImageId, List.of(itemId)));

        verify(post).markCompleted("/uploads/images/tryon/result.png");
        verify(postPort).save(post);
        verify(item).incrementTryCount();
        verify(itemPort).save(item);

        ArgumentCaptor<DeductTryonCreditUseCase.DeductTryonCreditCommand> creditCaptor =
                ArgumentCaptor.forClass(DeductTryonCreditUseCase.DeductTryonCreditCommand.class);
        verify(deductCreditUseCase).deductOnSuccess(creditCaptor.capture());
        assertThat(creditCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(creditCaptor.getValue().getJobId()).isEqualTo(jobId);

        verify(eventPublisher).publishEvent(any(TryonCompletedEvent.class));
    }

    @Test
    void process_whenAiGenerationFails_marksFailedWithoutCreditDeduction() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        BinaryImageStoragePort imageStoragePort = mock(BinaryImageStoragePort.class);
        DeductTryonCreditUseCase deductCreditUseCase = mock(DeductTryonCreditUseCase.class);
        SafeSearchPort safeSearchPort = mock(SafeSearchPort.class);
        TryonResultQualityPort tryonResultQualityPort = mock(TryonResultQualityPort.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getUserId()).thenReturn(userId);
        when(post.getPostId()).thenReturn(jobId);
        when(postPort.findById(jobId)).thenReturn(Optional.of(post));

        BaseImage baseImage = mock(BaseImage.class);
        when(baseImage.getUserId()).thenReturn(userId);
        when(baseImage.getImageUrl()).thenReturn("/uploads/images/tryon/base.png");
        when(baseImagePort.findById(baseImageId)).thenReturn(Optional.of(baseImage));

        ClosetItem item = mock(ClosetItem.class);
        ClothingImage clothingImage = mock(ClothingImage.class);
        when(item.getUserId()).thenReturn(userId);
        when(item.getClothingImage()).thenReturn(clothingImage);
        when(clothingImage.getImageUrl()).thenReturn("/uploads/images/closet/top.png");
        when(itemPort.findById(itemId)).thenReturn(Optional.of(item));

        when(imageReadPort.read("/uploads/images/tryon/base.png"))
                .thenReturn(new ImageReadPort.ImageReadResult("base".getBytes(), "image/png"));
        when(imageReadPort.read("/uploads/images/closet/top.png"))
                .thenReturn(new ImageReadPort.ImageReadResult("top".getBytes(), "image/png"));
        when(safeSearchPort.analyze(any()))
                .thenReturn(new SafeSearchPort.SafeSearchResult(
                        SafeSearchPort.Likelihood.VERY_UNLIKELY,
                        SafeSearchPort.Likelihood.VERY_UNLIKELY,
                        SafeSearchPort.Likelihood.VERY_UNLIKELY
                ));
        when(aiPort.generate(any())).thenThrow(new BusinessException(ErrorCode.AI_GENERATION_FAILED));

        TryonGenerationProcessor processor = new TryonGenerationProcessor(
                postPort,
                baseImagePort,
                itemPort,
                imageReadPort,
                aiPort,
                imageStoragePort,
                deductCreditUseCase,
                safeSearchPort,
                tryonResultQualityPort,
                eventPublisher
        );

        processor.process(new TryonGenerationRequestedEvent(userId, jobId, baseImageId, List.of(itemId)));

        verify(aiPort, times(1)).generate(any());
        verify(post).markFailed();
        verify(postPort).save(post);
        verify(post, never()).markCompleted(any());
        verify(deductCreditUseCase, never()).deductOnSuccess(any());
        verify(item, never()).incrementTryCount();
        verify(eventPublisher, never()).publishEvent(any(TryonCompletedEvent.class));
    }

    @Test
    void process_whenFirstResultFailsQuality_retriesAndSucceeds() {
        PostRepositoryPort postPort = mock(PostRepositoryPort.class);
        BaseImagePersistencePort baseImagePort = mock(BaseImagePersistencePort.class);
        ClosetItemPersistencePort itemPort = mock(ClosetItemPersistencePort.class);
        ImageReadPort imageReadPort = mock(ImageReadPort.class);
        TryonAiGenerationPort aiPort = mock(TryonAiGenerationPort.class);
        BinaryImageStoragePort imageStoragePort = mock(BinaryImageStoragePort.class);
        DeductTryonCreditUseCase deductCreditUseCase = mock(DeductTryonCreditUseCase.class);
        SafeSearchPort safeSearchPort = mock(SafeSearchPort.class);
        TryonResultQualityPort tryonResultQualityPort = mock(TryonResultQualityPort.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID baseImageId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Post post = mock(Post.class);
        when(post.getUserId()).thenReturn(userId);
        when(post.getPostId()).thenReturn(jobId);
        when(postPort.findById(jobId)).thenReturn(Optional.of(post));

        BaseImage baseImage = mock(BaseImage.class);
        when(baseImage.getUserId()).thenReturn(userId);
        when(baseImage.getImageUrl()).thenReturn("/uploads/images/tryon/base.png");
        when(baseImagePort.findById(baseImageId)).thenReturn(Optional.of(baseImage));

        ClosetItem item = mock(ClosetItem.class);
        ClothingImage clothingImage = mock(ClothingImage.class);
        when(item.getUserId()).thenReturn(userId);
        when(item.getClothingImage()).thenReturn(clothingImage);
        when(clothingImage.getImageUrl()).thenReturn("/uploads/images/closet/top.png");
        when(itemPort.findById(itemId)).thenReturn(Optional.of(item));

        when(imageReadPort.read("/uploads/images/tryon/base.png"))
                .thenReturn(new ImageReadPort.ImageReadResult("base".getBytes(), "image/png"));
        when(imageReadPort.read("/uploads/images/closet/top.png"))
                .thenReturn(new ImageReadPort.ImageReadResult("top".getBytes(), "image/png"));
        when(safeSearchPort.analyze(any()))
                .thenReturn(new SafeSearchPort.SafeSearchResult(
                        SafeSearchPort.Likelihood.VERY_UNLIKELY,
                        SafeSearchPort.Likelihood.VERY_UNLIKELY,
                        SafeSearchPort.Likelihood.VERY_UNLIKELY
                ));
        when(aiPort.generate(any()))
                .thenReturn(new TryonAiGenerationPort.TryonAiGenerationResult("result-1".getBytes(), "image/png"))
                .thenReturn(new TryonAiGenerationPort.TryonAiGenerationResult("result-2".getBytes(), "image/png"));
        when(tryonResultQualityPort.validate(any()))
                .thenReturn(new TryonResultQualityPort.TryonResultQualityResult(false, "PERSON_NOT_CENTERED"))
                .thenReturn(new TryonResultQualityPort.TryonResultQualityResult(true, "OK"));
        when(imageStoragePort.upload(any(), any(), any())).thenReturn("/uploads/images/tryon/result.png");

        TryonGenerationProcessor processor = new TryonGenerationProcessor(
                postPort,
                baseImagePort,
                itemPort,
                imageReadPort,
                aiPort,
                imageStoragePort,
                deductCreditUseCase,
                safeSearchPort,
                tryonResultQualityPort,
                eventPublisher
        );

        processor.process(new TryonGenerationRequestedEvent(userId, jobId, baseImageId, List.of(itemId)));

        verify(aiPort, times(2)).generate(any());
        verify(tryonResultQualityPort, times(2)).validate(any());
        verify(post).markCompleted("/uploads/images/tryon/result.png");
        verify(deductCreditUseCase).deductOnSuccess(any());
    }
}
