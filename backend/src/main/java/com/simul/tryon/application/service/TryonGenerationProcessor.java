package com.simul.tryon.application.service;

import com.simul.closet.application.port.out.ClosetItemPersistencePort;
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
import com.simul.tryon.domain.model.BaseImage;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TryonGenerationProcessor {

    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);

    private final PostRepositoryPort postRepositoryPort;
    private final BaseImagePersistencePort baseImagePersistencePort;
    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final ImageReadPort imageReadPort;
    private final TryonAiGenerationPort tryonAiGenerationPort;
    private final BinaryImageStoragePort binaryImageStoragePort;
    private final DeductTryonCreditUseCase deductTryonCreditUseCase;
    private final SafeSearchPort safeSearchPort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void process(TryonGenerationRequestedEvent event) {
        Post post = postRepositoryPort.findById(event.jobId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!event.userId().equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        try {
            BaseImage baseImage = baseImagePersistencePort.findById(event.baseImageId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            if (!event.userId().equals(baseImage.getUserId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            List<ClosetItem> items = event.itemIds().stream()
                    .map(itemId -> closetItemPersistencePort.findById(itemId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND)))
                    .toList();
            for (ClosetItem item : items) {
                if (!event.userId().equals(item.getUserId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN);
                }
            }

            runAiGenerationAndUpdatePost(post, baseImage, items);
        } catch (BusinessException be) {
            log.warn("TryOn generation failed: jobId={}, errorCode={}, detail={}", event.jobId(), be.getErrorCode(),
                    be.getMessage());
            post.markFailed();
            postRepositoryPort.save(post);
        } catch (Exception e) {
            log.error("TryOn generation failed (unexpected): jobId={}", event.jobId(), e);
            post.markFailed();
            postRepositoryPort.save(post);
        }
    }

    private void runAiGenerationAndUpdatePost(Post post, BaseImage baseImage, List<ClosetItem> items) {
        String userImageUrl = baseImage.getImageUrl();

        ImageReadPort.ImageReadResult userImage = imageReadPort.read(userImageUrl);
        ensureNotInappropriate(userImage.bytes());
        List<ImageReadPort.ImageReadResult> clothingImages = items.stream()
                .map(it -> imageReadPort.read(it.getClothingImage().getImageUrl()))
                .toList();
        for (ImageReadPort.ImageReadResult clothingImage : clothingImages) {
            ensureNotInappropriate(clothingImage.bytes());
        }

        String prompt = buildPromptForOrderedItems(clothingImages.size());

        List<TryonAiGenerationPort.ImagePart> clothingParts = clothingImages.stream()
                .map(img -> new TryonAiGenerationPort.ImagePart(img.bytes(), img.mimeType()))
                .toList();

        TryonAiGenerationPort.TryonAiGenerationCommand aiCommand = new TryonAiGenerationPort.TryonAiGenerationCommand(
                userImage.bytes(),
                userImage.mimeType(),
                clothingParts,
                prompt);

        TryonAiGenerationPort.TryonAiGenerationResult result = generateWithTimeout(aiCommand);

        String resultImageUrl = binaryImageStoragePort.upload(result.resultImageBytes(), result.resultImageMimeType(),
                "tryon");
        post.markCompleted(resultImageUrl);
        postRepositoryPort.save(post);

        // credit deduct only on success
        deductTryonCreditUseCase.deductOnSuccess(
                DeductTryonCreditUseCase.DeductTryonCreditCommand.builder()
                        .userId(post.getUserId())
                        .jobId(post.getPostId())
                        .build());

        // increment try_count for each used item
        for (ClosetItem item : items) {
            item.incrementTryCount();
            closetItemPersistencePort.save(item);
        }

        // 시착 완료 알림 이벤트 발행
        eventPublisher.publishEvent(new TryonCompletedEvent(post.getUserId(), post.getPostId()));
    }

    private void ensureNotInappropriate(byte[] imageBytes) {
        SafeSearchPort.SafeSearchResult safeSearch = safeSearchPort.analyze(imageBytes);
        if (safeSearch.isInappropriate()) {
            throw new BusinessException(ErrorCode.INAPPROPRIATE_IMAGE);
        }
    }

    private TryonAiGenerationPort.TryonAiGenerationResult generateWithTimeout(
            TryonAiGenerationPort.TryonAiGenerationCommand command) {
        try {
            return CompletableFuture.supplyAsync(() -> tryonAiGenerationPort.generate(command))
                    .orTimeout(AI_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause();
            if (cause instanceof java.util.concurrent.TimeoutException) {
                throw new BusinessException(ErrorCode.AI_TIMEOUT);
            }
            if (cause instanceof BusinessException be) {
                throw be;
            }
            throw ce;
        }
    }

    private String buildPromptForOrderedItems(int clothingCount) {
        String base = """
                You are a professional virtual try-on image compositor.

                ## Inputs
                - Image 1: A photo of a person (the model).
                - Image 2+: One or more clothing item images to be worn by the person, in the given order.

                ## Your Task
                Generate a single, photorealistic image of the person naturally wearing all provided clothing items.

                ## Critical Requirements

                ### Person Preservation (highest priority)
                - Preserve the person's face, skin tone, hair, and body proportions EXACTLY as they appear in Image 1.
                - Preserve the person's original pose and body position. Do not alter limb placement or stance.
                - Do not alter facial features, expression, age, or identity in any way.

                ### Person Positioning
                - Place the person at the horizontal and vertical center of the image.
                - The person should be fully visible and not cropped at the edges.
                - Maintain natural proportions — do not stretch, shrink, or distort the person to fit the frame.
                - Adjust the background to fill the remaining canvas space naturally if repositioning is needed.

                ### Clothing Rendering
                - Realistically drape and fit each clothing item onto the person's body, respecting gravity, body shape, and natural fabric behavior (wrinkles, folds, shadows).
                - Match the clothing's color, texture, pattern, and material exactly as shown in the clothing images. Do not alter colors or prints.
                - If multiple clothing items are provided, layer them naturally (e.g., top over bottom, outerwear over inner layers).
                - Occlude clothing items correctly behind arms, hands, and other body parts where appropriate.

                ### Background & Lighting
                - Preserve the original background from Image 1 exactly. Do not change, blur, or replace it.
                - If the background must be extended to accommodate centering, extend it naturally to match the original background style and color.
                - Match lighting and shadows on the clothing to the lighting conditions in Image 1.
                - Do not add any text, watermarks, logos, or UI elements to the output image.

                ### Output
                - Return one final composited image only.
                - The image should look like a natural, professional photograph — not a collage or montage.
                """;
        return switch (clothingCount) {
            case 1 -> base + "\nOrder: 2nd image = clothing item.\n";
            case 2 -> base + "\nOrder: 2nd image = top, 3rd image = bottom.\n";
            case 3 -> base + "\nOrder: 2nd image = top, 3rd image = bottom, 4th image = accessory.\n";
            default -> base;
        };
    }
}
