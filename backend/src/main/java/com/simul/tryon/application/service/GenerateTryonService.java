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
import com.simul.tryon.application.port.out.TryonCreditPersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return new TryonGenerateResponse(saved.getPostId(), "processing", ESTIMATED_SECONDS);
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

