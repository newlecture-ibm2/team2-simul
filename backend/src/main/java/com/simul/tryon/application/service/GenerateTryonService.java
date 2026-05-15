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
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.dto.TryonGenerationRequestedEvent;
import com.simul.tryon.application.dto.TryonGenerateResponse;
import com.simul.tryon.application.port.in.GenerateTryonUseCase;
import com.simul.tryon.application.port.in.DeductTryonCreditUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.application.port.out.SafeSearchPort;
import com.simul.tryon.application.port.out.TryonAiGenerationPort;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GenerateTryonService implements GenerateTryonUseCase {

    private static final int TOTAL_DAILY = 5;
    private static final int ESTIMATED_SECONDS = 20;
    private static final int MAX_ITEM_IDS = 3;
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRY = 1;

    private final BaseImagePersistencePort baseImagePersistencePort;
    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final PostRepositoryPort postRepositoryPort;
    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;
    private final ImageReadPort imageReadPort;
    private final TryonAiGenerationPort tryonAiGenerationPort;
    private final BinaryImageStoragePort binaryImageStoragePort;
    private final DeductTryonCreditUseCase deductTryonCreditUseCase;
    private final SafeSearchPort safeSearchPort;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${simul.gemini.enabled:false}")
    private boolean geminiEnabled;

    @Override
    public TryonGenerateResponse generate(GenerateTryonCommand command) {
        if (command.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (command.getBaseImageId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "base_image_id는 필수입니다.");
        }

        List<UUID> itemIds = normalizeItemIds(command);
        if (itemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "item_ids(또는 item_id)는 최소 1개 필요합니다.");
        }
        if (itemIds.size() > MAX_ITEM_IDS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "item_ids는 최대 3개까지 지원합니다.");
        }

        ensureCreditsRemaining(command.getUserId());
        BaseImage baseImage = loadAndValidateBaseImage(command.getUserId(), command.getBaseImageId());
        List<ClosetItem> items = itemIds.stream()
                .map(itemId -> loadAndValidateItem(command.getUserId(), itemId))
                .toList();

        Post saved = postRepositoryPort.save(Post.builder()
                .userId(command.getUserId())
                .baseImageId(baseImage.getId())
                // For now, link the first item as the primary clothing source.
                .itemId(items.getFirst().getId())
                .status(PostStatus.PROCESSING)
                .isPublic(false)
                .build());

        // Respond immediately with job_id and run AI generation asynchronously after transaction commit.
        if (geminiEnabled) {
            eventPublisher.publishEvent(new TryonGenerationRequestedEvent(
                    saved.getUserId(),
                    saved.getPostId(),
                    baseImage.getId(),
                    itemIds
            ));
        }

        return new TryonGenerateResponse(saved.getPostId(), "processing", ESTIMATED_SECONDS);
    }

    private List<UUID> normalizeItemIds(GenerateTryonCommand command) {
        if (command.getItemIds() != null && !command.getItemIds().isEmpty()) {
            if (command.getItemIds().stream().anyMatch(it -> it == null)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "item_ids에 null이 포함될 수 없습니다.");
            }
            return command.getItemIds();
        }
        if (command.getItemId() != null) {
            return List.of(command.getItemId());
        }
        return List.of();
    }

    private void runAiGenerationAndUpdatePost(Post post, BaseImage baseImage, List<ClosetItem> items) {
        // Lazily loaded, but within transactional boundary
        String userImageUrl = baseImage.getImageUrl();

        ImageReadPort.ImageReadResult userImage = imageReadPort.read(userImageUrl);
        ensureNotInappropriate(post, userImage.bytes());
        List<ImageReadPort.ImageReadResult> clothingImages = items.stream()
                .map(it -> imageReadPort.read(it.getClothingImage().getImageUrl()))
                .toList();
        for (ImageReadPort.ImageReadResult clothingImage : clothingImages) {
            ensureNotInappropriate(post, clothingImage.bytes());
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

        try {
            TryonAiGenerationPort.TryonAiGenerationResult result = generateWithRetry(aiCommand);

            String resultImageUrl = binaryImageStoragePort.upload(result.resultImageBytes(),
                    result.resultImageMimeType(), "tryon");
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
        } catch (BusinessException be) {
            post.markFailed();
            postRepositoryPort.save(post);
            throw be;
        } catch (Exception e) {
            post.markFailed();
            postRepositoryPort.save(post);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, e.getMessage());
        }
    }

    private void ensureNotInappropriate(Post post, byte[] imageBytes) {
        SafeSearchPort.SafeSearchResult safeSearch = safeSearchPort.analyze(imageBytes);
        if (safeSearch.isInappropriate()) {
            post.markFailed();
            postRepositoryPort.save(post);
            throw new BusinessException(ErrorCode.INAPPROPRIATE_IMAGE);
        }
    }

    private TryonAiGenerationPort.TryonAiGenerationResult generateWithRetry(
            TryonAiGenerationPort.TryonAiGenerationCommand command) {
        BusinessException lastBusinessException = null;

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                return generateWithTimeout(command);
            } catch (BusinessException be) {
                lastBusinessException = be;
                if (shouldRetry(be) && attempt < MAX_RETRY) {
                    continue;
                }
                throw be;
            }
        }

        // Should be unreachable
        throw lastBusinessException != null ? lastBusinessException
                : new BusinessException(ErrorCode.AI_GENERATION_FAILED);
    }

    private boolean shouldRetry(BusinessException be) {
        // 재시도는 비용(외부 AI 호출) 중복 청구 위험이 큼.
        // Timeout 같이 네트워크/일시 장애로 판단 가능한 케이스만 제한적으로 재시도한다.
        return be.getErrorCode() == ErrorCode.AI_TIMEOUT;
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

    private void ensureCreditsRemaining(UUID userId) {
        LocalDate todayKst = LocalDate.now(kstClock);
        LocalDateTime start = todayKst.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long usedCount = tryonCreditPersistencePort.countByUserIdAndUsedAtBetween(userId, start, end);
        if (usedCount >= TOTAL_DAILY) {
            throw new BusinessException(ErrorCode.CREDIT_EXHAUSTED);
        }
    }

    private BaseImage loadAndValidateBaseImage(UUID userId, UUID baseImageId) {
        BaseImage baseImage = baseImagePersistencePort.findById(baseImageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!userId.equals(baseImage.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return baseImage;
    }

    private ClosetItem loadAndValidateItem(UUID userId, UUID itemId) {
        ClosetItem item = closetItemPersistencePort.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!userId.equals(item.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return item;
    }
}
