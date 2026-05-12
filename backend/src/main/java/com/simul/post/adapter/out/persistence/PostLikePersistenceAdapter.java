package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.PostLikePersistencePort;
import com.simul.post.domain.model.PostLike;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PostLikePersistenceAdapter implements PostLikePersistencePort {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId) {
        return postLikeJpaRepository.findByPostIdAndUserId(postId, userId);
    }

    @Override
    public PostLike save(PostLike postLike) {
        return postLikeJpaRepository.save(postLike);
    }

    @Override
    public void delete(PostLike postLike) {
        postLikeJpaRepository.delete(postLike);
    }

    @Override
    public Set<UUID> findLikedPostIdsByUserIdAndPostIds(UUID userId, List<UUID> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(postLikeJpaRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds));
    }

    @Override
    public Page<PostLike> findByPostId(UUID postId, Pageable pageable) {
        return postLikeJpaRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    }
}
