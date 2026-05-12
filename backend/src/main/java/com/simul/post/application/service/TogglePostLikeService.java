package com.simul.post.application.service;

import com.simul.notification.application.dto.PostLikedEvent;
import com.simul.post.application.dto.ToggleLikeResponse;
import com.simul.post.application.port.in.TogglePostLikeUseCase;
import com.simul.post.application.port.out.PostLikePersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostLike;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * [Hexagonal - Application Service]
 * 좋아요 토글 유스케이스 구현
 *
 * - 좋아요 존재 시: 삭제 + likeCount 감소
 * - 좋아요 미존재 시: 생성 + likeCount 증가
 */
@Service
@RequiredArgsConstructor
public class TogglePostLikeService implements TogglePostLikeUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final PostLikePersistencePort postLikePersistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ToggleLikeResponse toggleLike(UUID postId, UUID userId) {
        // 1. 게시물 존재 여부 확인
        Post post = postRepositoryPort.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("ERR-003: 해당 게시물을 찾을 수 없습니다."));

        // 2. 기존 좋아요 여부 확인
        Optional<PostLike> existingLike = postLikePersistencePort.findByPostIdAndUserId(postId, userId);

        boolean isLiked;

        if (existingLike.isPresent()) {
            // 3a. 이미 좋아요 → 취소 (Unlike)
            postLikePersistencePort.delete(existingLike.get());
            post.decrementLikeCount();
            isLiked = false;
        } else {
            // 3b. 좋아요 없음 → 생성 (Like)
            PostLike newLike = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postLikePersistencePort.save(newLike);
            post.incrementLikeCount();
            isLiked = true;
        }

        // 4. 변경된 likeCount 반영
        postRepositoryPort.save(post);

        // 5. 좋아요 시에만 알림 이벤트 발행 (좋아요 취소 시에는 알림 안 보냄)
        if (isLiked) {
            eventPublisher.publishEvent(new PostLikedEvent(userId, post.getUserId(), postId));
        }

        return new ToggleLikeResponse(isLiked, post.getLikeCount());
    }
}
