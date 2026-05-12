package com.simul.post.application.port.out;

import com.simul.post.domain.model.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * [Hexagonal - Output Port]
 * 좋아요 영속성 포트
 */
public interface PostLikePersistencePort {

    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    PostLike save(PostLike postLike);

    void delete(PostLike postLike);

    /**
     * 특정 게시물 목록에 대해 해당 유저가 좋아요한 게시물 ID 집합 반환
     * (피드 목록에서 isLiked 여부를 일괄 조회할 때 사용)
     */
    Set<UUID> findLikedPostIdsByUserIdAndPostIds(UUID userId, java.util.List<UUID> postIds);

    /**
     * 특정 게시물에 좋아요를 누른 내역 조회 (페이징)
     */
    Page<PostLike> findByPostId(UUID postId, Pageable pageable);
}
