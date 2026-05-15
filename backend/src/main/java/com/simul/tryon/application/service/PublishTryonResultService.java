package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import com.simul.tryon.application.port.in.PublishTryonResultUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PublishTryonResultService implements PublishTryonResultUseCase {

    private final PostRepositoryPort postRepositoryPort;

    @Override
    public void publish(PublishTryonResultCommand command) {
        if (command.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (command.getJobId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "job_id는 필수입니다.");
        }

        Post post = postRepositoryPort.findById(command.getJobId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!command.getUserId().equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (post.getStatus() != PostStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "완료된 시착 결과만 피드에 공유할 수 있습니다.");
        }
        if (post.getImageUrl() == null || post.getImageUrl().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시착 결과 이미지가 존재하지 않습니다.");
        }

        post.update(null, true);
        postRepositoryPort.save(post);
    }
}
