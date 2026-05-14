package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.dto.TryonJobResponse;
import com.simul.tryon.application.port.in.GetTryonJobUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import com.simul.tryon.domain.model.BaseImage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTryonJobService implements GetTryonJobUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final BaseImagePersistencePort baseImagePersistencePort;

    @Override
    public TryonJobResponse getJob(GetTryonJobQuery query) {
        if (query.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (query.getJobId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "job_id는 필수입니다.");
        }

        Post post = postRepositoryPort.findById(query.getJobId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!query.getUserId().equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        UUID baseImageId = post.getBaseImageId();
        String baseImageUrl = null;
        if (baseImageId != null) {
            BaseImage baseImage = baseImagePersistencePort.findById(baseImageId)
                    .orElse(null);
            if (baseImage != null) {
                baseImageUrl = baseImage.getImageUrl();
            }
        }

        String status = mapStatus(post.getStatus());
        String resultImageUrl = post.getStatus() == PostStatus.COMPLETED ? post.getImageUrl() : null;

        return new TryonJobResponse(post.getPostId(), status, baseImageUrl, resultImageUrl);
    }

    private String mapStatus(PostStatus status) {
        if (status == PostStatus.COMPLETED) return "completed";
        if (status == PostStatus.FAILED) return "failed";
        return "processing";
    }
}

