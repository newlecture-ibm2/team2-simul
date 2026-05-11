package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.dto.BaseImageUploadResponse;
import com.simul.tryon.application.port.in.RegisterBaseImageFromPostUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterBaseImageFromPostService implements RegisterBaseImageFromPostUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final BaseImagePersistencePort baseImagePersistencePort;

    @Override
    public BaseImageUploadResponse register(RegisterBaseImageFromPostCommand command) {
        if (command.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (command.getSourcePostId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "source_post_id는 필수입니다.");
        }

        Post post = postRepositoryPort.findById(command.getSourcePostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(command.getUserId(), post);
        validateCompleted(post);
        validatePostHasImage(post);

        BaseImage saved = baseImagePersistencePort.save(
                new BaseImage(command.getUserId(), post.getImageUrl(), post.getPostId())
        );

        return new BaseImageUploadResponse(saved.getId(), saved.getImageUrl());
    }

    private void validateOwnership(UUID userId, Post post) {
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateCompleted(Post post) {
        if (post.getStatus() != PostStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "완료된 시착 결과만 베이스 이미지로 등록할 수 있습니다.");
        }
    }

    private void validatePostHasImage(Post post) {
        if (post.getImageUrl() == null || post.getImageUrl().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시착 결과 이미지가 존재하지 않습니다.");
        }
    }
}

