package com.simul.tryon.application.service;

import com.simul.closet.application.port.out.ClosetItemPersistencePort;
import com.simul.closet.domain.model.ClosetItem;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.dto.TryonGenerateResponse;
import com.simul.tryon.application.port.in.GenerateTryonUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.application.port.out.TryonAiGenerationPort;
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simul.common.application.port.out.ImageReadPort;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Transactional
public class GenerateTryonService implements GenerateTryonUseCase {

    private static final int TOTAL_DAILY = 5;
    private static final int ESTIMATED_SECONDS = 20;

    private final BaseImagePersistencePort baseImagePersistencePort;
    private final ClosetItemPersistencePort closetItemPersistencePort;
    private final PostRepositoryPort postRepositoryPort;
    private final TryonCreditPersistencePort tryonCreditPersistencePort;
    private final Clock kstClock;
    private final ImageReadPort imageReadPort;
    private final TryonAiGenerationPort tryonAiGenerationPort;

    @Value("${simul.gemini.enabled:false}")
    private boolean geminiEnabled;

    @Override
    public TryonGenerateResponse generate(GenerateTryonCommand command) {
        if (command.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (command.getBaseImageId() == null || command.getItemId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "base_image_id와 item_id는 필수입니다.");
        }

        ensureCreditsRemaining(command.getUserId());
        BaseImage baseImage = loadAndValidateBaseImage(command.getUserId(), command.getBaseImageId());
        ClosetItem item = loadAndValidateItem(command.getUserId(), command.getItemId());

        Post saved = postRepositoryPort.save(Post.builder()
                .userId(command.getUserId())
                .baseImageId(baseImage.getId())
                .itemId(item.getId())
                .status(PostStatus.PROCESSING)
                .isPublic(false)
                .build());

        // NOTE: In current phase, we call Gemini synchronously for a single-step generation.
        // This will be evolved into async pipeline + SSE in subsequent tasks.
        if (geminiEnabled) {
            runAiGenerationAndUpdatePost(saved, baseImage, item);
        }

        return new TryonGenerateResponse(saved.getPostId(), "processing", ESTIMATED_SECONDS);
    }

    private void runAiGenerationAndUpdatePost(Post post, BaseImage baseImage, ClosetItem item) {
        // Lazily loaded, but within transactional boundary
        String userImageUrl = baseImage.getImageUrl();
        String clothingImageUrl = item.getClothingImage().getImageUrl();

        ImageReadPort.ImageReadResult userImage = imageReadPort.read(userImageUrl);
        ImageReadPort.ImageReadResult clothingImage = imageReadPort.read(clothingImageUrl);

        String prompt = """
                You are a virtual try-on image generator.
                - The first image is a photo of a person.
                - The second image is a clothing item image.
                Task:
                - Generate a realistic image of the person wearing the clothing item.
                Constraints:
                - Preserve the person’s identity and pose as much as possible.
                - Keep the background natural (do not add text).
                Output:
                - Return only the generated image.
                """;

        TryonAiGenerationPort.TryonAiGenerationResult result = tryonAiGenerationPort.generate(
                new TryonAiGenerationPort.TryonAiGenerationCommand(
                        userImage.bytes(),
                        userImage.mimeType(),
                        clothingImage.bytes(),
                        clothingImage.mimeType(),
                        prompt
                )
        );

        // TODO: persist result image into storage and set post.imageUrl + status=COMPLETED
        // That will be added once image output persistence path is finalized.
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
